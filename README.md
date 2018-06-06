Spring Data Hazelcast
=======================
Welcome to Spring Data Hazelcast project!

The primary goal of the [Spring Data](http://projects.spring.io/spring-data/) project is to make it easier to build Spring-powered applications that use new data access technologies. This module provides integration with [Hazelcast](http://hazelcast.com).

# Examples

For examples on using Spring Data Hazelcast, see dedicated Code Samples: [spring-data-hazelcast-chemistry-sample](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/spring-data-hazelcast-chemistry-sample) and [spring-data-jpa-hazelcast-migration](https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/spring-data-jpa-hazelcast-migration).

# Artifacts

## Maven

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>spring-data-hazelcast</artifactId>
    <version>${version}</version>
</dependency>
```

## Gradle

```groovy
dependencies {
    compile 'com.hazelcast:spring-data-hazelcast:${version}'
}
```

# Usage

## Spring Configuration

```java
@Configuration
@EnableHazelcastRepositories(basePackages={"example.springdata.keyvalue.chemistry"}) // <1>
public class ApplicationConfiguration {
    @Bean
    HazelcastInstance hazelcastInstance() {     // <2> 
        return Hazelcast.newHazelcastInstance();
        // return HazelcastClient.newHazelcastClient();
    }
}
```

1. Enables Spring Data magic for Hazelcast. You can specify `basePackages` for component scan.
2. Instantiates Hazelcast instance (a member or a client)

## Repository Definition

```java
public interface SpeakerRepository extends HazelcastRepository<Speaker, Long> {}
```

## Test of Repository

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfiguration.class)
public class AppTest {
    @Autowired
    SpeakerRepository speakerRepository;

    @Test
    public void testStart(){
        speakerRepository.findAll();
    }
}
```

# @Query Support

## Sample @Query Usages

### Query with hardcoded value

```java
@Query("firstname=James")
public List<Person> peoplewiththeirFirstNameIsJames();
```

### Query with one variable

```java
@Query("firstname=%s")
public List<Person> peoplewiththeirFirstName(String firstName);
```

### Query with multiple variable values

```java
@Query("firstname=%s and lastname=%s")
public List<Person> peoplewithFirstAndLastName(String firstName,String lastName);
```

## Supported Query Keywords

```
True
False
Equal
NotEqual
Before
LessThan
LessThanEqual
After
GreaterThan
GreaterThanEqual
Between
IsNull
IsNotNull
In
NotIn
Containing
NotContaining
StartingWith
EndingWith
Like
NotLike
Regex
```

# Community

## Chat with developers

[![Gitter](https://badges.gitter.im/hazelcast/spring-data-hazelcast.svg)](https://gitter.im/hazelcast/hazelcast)

## Stack Overflow 

[![Stack Overflow](http://cdn.sstatic.net/Sites/stackoverflow/company/img/logos/so/so-icon.png?v=c78bd457575a)](http://stackoverflow.com/questions/tagged/hazelcast)

Submit your question with `hazelcast` and `spring-data-hazelcast` tags.

## Mail Group

Please, [join the mail group](http://groups.google.com/group/hazelcast) if you are interested in using or developing Hazelcast.

# License

Hazelcast is available under the Apache 2 License. Please see the Licensing appendix for more information.

# Copyright

Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.

Visit http://www.hazelcast.com for more information.

