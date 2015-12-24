/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast.repository.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.StreamUtils;

/**
 * <P>There is one instance for each query method defined for a repository,
 * providing a query from the bind parameters.
 * </P>
 * <P>
 * TODO The {@link #execute} method calls the {@link #prepareQuery} to
 * bind the parameters to the query. This results in Hazelcast's
 * {@link com.hazelcast.query.PredicateBuilder PredicateBuilder}
 * being called for each query execution. A more efficient mechanism
 * would be to some sort of templating that would allow the parameters
 * to be instantiated into the output of the predicate builder rather
 * than the input.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastPartTreeQuery extends KeyValuePartTreeQuery {

    private final QueryMethod queryMethod;
    private final KeyValueOperations keyValueOperations;

    private boolean isCount;
    private boolean isDelete;
    private boolean isDistinct;

    private boolean isRearrangeKnown;
    private boolean isRearrangeRequired;
    private int[]   rearrangeIndex;

    /**
     * <P>Create a {@link RepositoryQuery} implementation for each query method
     * defined in a {@link HazelcastRepository}.
     * </P>
     *
     * @param queryMethod                   Method defined in {@code HazelcastRepository}
     * @param evalulationContextProvider    Not used
     * @param keyValueOperations            Interface to Hazelcast
     * @param queryCreator                  Not used
     */
    public HazelcastPartTreeQuery(QueryMethod queryMethod,
            EvaluationContextProvider evaluationContextProvider,
            KeyValueOperations keyValueOperations,
            Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
        super(queryMethod, evaluationContextProvider, keyValueOperations, queryCreator);
        this.queryMethod = queryMethod;
        this.keyValueOperations = keyValueOperations;

        this.isRearrangeKnown = false;
    }

    /**
     * <P>Execute this query instance, using any invocation parameters.
     * </P>
     * <P>Expecting {@code findBy...()}, {@code countBy...()} or {@code deleteBy...()}
     * </P>
     *
     * @param parameters Any parameters
     * @return           Query result
     */
    @Override
    public Object execute(Object[] parameters) {

        KeyValueQuery<?> query = prepareQuery(parameters);

        /* Distinct modifier could be implemented using Aggregations.
         */
        if (this.isDistinct) {
            String message = String.format("Distinct in '%s' not yet supported.", queryMethod.getName());
            throw new UnsupportedOperationException(message);
        }

        if (this.isCount) {
            return this.keyValueOperations.count(query, queryMethod.getEntityInformation().getJavaType());
        }

        /* delete(KeyValueQuery<?> query, Class<?> type) is not part of KeyValueOperations
         * interface, although Hazelcast could do IMap.values().removeIf(predicate);
         */
        if (this.isDelete) {
            String message = String.format("Delete in '%s' not yet supported.", queryMethod.getName());
            throw new UnsupportedOperationException(message);
        }

        if (queryMethod.isPageQuery()
                || queryMethod.isSliceQuery()) {
                return this.executePageSliceQuery(parameters, query, queryMethod);
        }

        if (queryMethod.isCollectionQuery()
                || queryMethod.isQueryForEntity()
                || queryMethod.isStreamQuery()) {
                return this.executeFindQuery(query, queryMethod);
        }

        String message = String.format("Query method '%s' not supported.", queryMethod.getName());
        throw new UnsupportedOperationException(message);
    }

    /**
     * <P>Execute a retrieval query. The query engine will return this in an iterator,
     * which may need conversion to a single domain entity or a stream.
     * </P>
     *
     * @param query          The query to run
     * @param queryMethod    Holds metadata about the query, is paging etc
     * @return               Query result
     */
    private Object executeFindQuery(final KeyValueQuery<?> query, final QueryMethod queryMethod) {

        Iterable<?> resultSet =
                this.keyValueOperations.find(query, queryMethod.getEntityInformation().getJavaType());

        if (!queryMethod.isCollectionQuery() && !queryMethod.isPageQuery()
                && !queryMethod.isSliceQuery() && !queryMethod.isStreamQuery()) {
            // Singleton result
            return resultSet.iterator().hasNext() ? resultSet.iterator().next() : null;
        }

        if (queryMethod.isStreamQuery()) {
            return StreamUtils.createStreamFromIterator(resultSet.iterator());
        }

        return resultSet;
    }

    /**
     * <P>Slices and pages are similar ways to iterate through the result set in
     * blocks, mimicking a cursor. A {@link org.springframework.data.domain.Slice Slice}
     * is a simpler concept, only requiring to know if further blocks of data are available.
     * A {@link org.springframework.data.domain.Page Page}
     * requires to know how many blocks of data are available in total.
     * </P>
     *
     * @param parameters     For the query
     * @param query          The query to run
     * @param queryMethod    Holds metadata about the query
     * @return               Query result
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object executePageSliceQuery(final Object[] parameters, final KeyValueQuery<?> query, final QueryMethod queryMethod) {
        long totalElements = -1;

        int indexOfPageRequest = queryMethod.getParameters().getPageableIndex();
        Pageable pageRequest = (Pageable) parameters[indexOfPageRequest];

        /* TODO Eliminate count call for Slice, retrieve "rows+1" instead to determine if next page exists.
         */
        if (query.getCritieria() == null) {
            totalElements = this.keyValueOperations.count(queryMethod.getEntityInformation().getJavaType());
        } else {
            totalElements = this.keyValueOperations.count(query, queryMethod.getEntityInformation().getJavaType());
        }

        int requiredRows = pageRequest.getPageSize();

        query.setOffset(pageRequest.getOffset());
        query.setRows(pageRequest.getPageSize());

        Iterable<?> resultSet =
                this.keyValueOperations.find(query, queryMethod.getEntityInformation().getJavaType());
        List<?> content = IterableConverter.toList(resultSet);

        if (queryMethod.isPageQuery()) {
            return new PageImpl(content, pageRequest, totalElements);
        } else {
            boolean hasNext = totalElements > (query.getOffset() + query.getRows());
            if (content.size() > requiredRows) {
                content = content.subList(0, requiredRows);
            }
            return new SliceImpl(content, pageRequest, hasNext);
        }
    }

    /**
     * <P>Create the query from the bind parameters.
     * </P>
     *
     * @param parameters Possibly empty list of query parameters
     * @return           A ready-to-use query
     */
    private KeyValueQuery<?> prepareQuery(Object[] parameters) {
        PartTree tree = null;

        if (this.queryMethod.getParameters().getNumberOfParameters() > 0) {
            tree = new PartTree(getQueryMethod().getName(), getQueryMethod().getEntityInformation().getJavaType());
            this.isCount = tree.isCountProjection();
            this.isDelete = tree.isDelete();
            this.isDistinct = tree.isDistinct();
        } else {
            this.isCount = false;
            this.isDelete = false;
            this.isDistinct = false;
        }

        ParametersParameterAccessor accessor = this.prepareAccessor(parameters, tree);

        KeyValueQuery<?> query = createQuery(accessor);

        if (accessor.getPageable() != null) {
            query.setOffset(accessor.getPageable().getOffset());
            query.setRows(accessor.getPageable().getPageSize());
        } else {
            query.setOffset(-1);
            query.setRows(-1);
        }

        if (accessor.getSort() != null) {
            query.setSort(accessor.getSort());
        }

        return query;
    }

    /**
     * <P>Handle {@code @Param}.
     * </P>
     * <OL>
     * <LI><B>Without {@code @Param}</B>
     * <P>Arguments to the call are assumed to follow the same sequence as cited
     * in the method name.</P>
     * <P>Eg.<pre>findBy<U>One</U>And<U>Two</U>(String <U>one</U>, String <U>two</U>);</pre></P>
     * </LI>
     * <LI><B>With {@code @Param}</B>
     * <P>Arguments to the call are use the {@code @Param} to match them against the fields.
     * <P>Eg.<pre>findBy<U>One</U>And<U>Two</U>(@Param("two") String <U>two</U>, @Param("one") String <U>one</U>);</pre></P>
     * </LI>
     * </OL>
     *
     * @param parameters Possibly empty
     * @param partTree   Query tree to traverse
     * @return           Paremeters in correct order
     */
    private ParametersParameterAccessor prepareAccessor(final Object[] originalParameters, final PartTree partTree) {

        if (!this.isRearrangeKnown) {
            this.prepareRearrange(partTree, this.queryMethod.getParameters().getBindableParameters());
            this.isRearrangeKnown = true;
        }

        Object[] parameters = originalParameters;
        if (parameters != null && this.isRearrangeRequired) {
            parameters = new Object[originalParameters.length];

            for (int i = 0 ; i < parameters.length; i++) {
                int index = rearrangeIndex[i];
                parameters[i] = originalParameters[index];
            }
        }

        return new ParametersParameterAccessor(this.queryMethod.getParameters(), parameters);
    }

    /**
     * <P>Determine if the arguments to the method need reordered.
     * </P>
     * <P>For searches such as {@code findBySomethingNotNull} there may
     * be more parts than parameters needed to be bound to them.
     * </P>
     *
     * @param partTree           Query parts
     * @param bindableParameters Parameters expected
     */
    @SuppressWarnings("unchecked")
    private void prepareRearrange(final PartTree partTree, final Parameters<?, ?> bindableParameters) {

        this.isRearrangeRequired = false;
        if (partTree == null || bindableParameters == null) {
            return;
        }

        List<String> queryParams = new ArrayList<>();
        List<String> methodParams = new ArrayList<>();

        Iterator<Part> partTreeIterator = partTree.getParts().iterator();
        while (partTreeIterator.hasNext()) {
            Part part = partTreeIterator.next();
            queryParams.add(part.getProperty().getSegment());
        }

        Iterator<Parameter> bindableParameterIterator = (Iterator<Parameter>) bindableParameters.iterator();
        while (bindableParameterIterator.hasNext()) {
            Parameter parameter = bindableParameterIterator.next();
            methodParams.add(parameter.getName());
        }

        this.rearrangeIndex = new int[queryParams.size()];

        String[] paramsExpected = queryParams.toArray(new String[queryParams.size()]);
        String[] paramsProvided = methodParams.toArray(new String[methodParams.size()]);

        for (int i = 0 ; i < this.rearrangeIndex.length ; i++) {
            this.rearrangeIndex[i] = i;

            for (int j = 0 ; j < paramsProvided.length ; j++) {
                if (paramsProvided[j] != null && paramsProvided[j].equals(paramsExpected[i])) {
                    this.rearrangeIndex[i] = j;
                    this.isRearrangeRequired = true;
                }
            }
        }
    }

}
