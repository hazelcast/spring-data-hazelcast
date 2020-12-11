# Spring Data Hazelcast

<a href="https://github.com/hazelcast/spring-data-hazelcast/actions?query=event%3Apush+branch%3Amaster"><img alt="GitHub Actions status" src="https://github.com/hazelcast/spring-data-hazelcast/workflows/build/badge.svg"></a>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hazelcast/spring-data-hazelcast/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hazelcast/spring-data-hazelcast) 

The primary goal of the [Spring Data](http://projects.spring.io/spring-data/) is to make it easier to build Spring-powered applications that use new data access technologies. This module provides integration with [Hazelcast](http://hazelcast.com).

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
Distinct
IsEmpty
ExistsBy
IsWithin
IsNear
```

