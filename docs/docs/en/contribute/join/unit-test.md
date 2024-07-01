## Unit Test Coverage

### 1. The Benefits of Writing Unit Tests

- Unit tests help everyone to get into the details of the code and understand how it works.
- Through test cases we can find bugs and submit robust code.
- The test case is also a demo usage of the code.

### 2. Some design principles for unit test cases

- The steps, granularity and combination of conditions should be carefully designed.
- Pay attention to boundary conditions.
- Unit tests should be well designed as well as avoiding useless code.
- When you find a `method` is difficult to write unit test, and if you confirm that the `method` is `bad code`, then refactor it with the developer.

<!-- markdown-link-check-disable -->
- DolphinScheduler: [mockito](http://site.mockito.org/). Here are some development guides: [mockito tutorial](http://www.baeldung.com/bdd-mockito), [mockito refcard](https://dzone.com/refcardz/mockito)

<!-- markdown-link-check-enable -->
- TDD(option): When you start writing a new feature, you can try writing test cases first.

### 3. Test coverage setpoint

- At this stage, the default value for test coverage of Delta change codes is >= 60%, the higher the better.
- We can see the test reports on this page:  https://codecov.io/gh/apache/dolphinscheduler

## Fundamental guidelines for unit test

### 1. Isolation and singleness

A test case should be accurate to the method level, and it should be possible to execute the test case alone. At the same time the focus is always on the method (only the method is tested).

If the method is too complex, it should be split up again during the development phase. For test cases, it is best that a case focuses on only one branch (judgment). When changes are applied to it, they only affect the success of a test case. This will greatly facilitate our verification of issues and problem solving during the development phase. At the same time, however, it also poses a great challenge in terms of coverage.

### 2. Automaticity

Unit tests can be automated. Mandatory: all unit tests must be written under src/test. Also the method naming should conform to the specification. Benchmark tests are excluded.

### 3. reproducibility

Multiple executions (any environment, any time) result in unique and repeatable results.

### 4. Lightweight

That is, any environment can be implemented quickly.

This requires that we don't rely on too many components, such as various spring beans and the like. These are all mock in unit tests, and adding them would increase the speed of our single-test execution, as well as potentially passing on contamination.

For some databases, other external components, etc. As far as possible, the mock client is not dependent on the external environment (the presence of any external dependencies greatly limits the portability and stability of test cases and the correctness of results), which also makes it easy for developers to test in any environment.

### 5. Measurable

Over the years, mockito has grown to be the NO.1 mock, but it still doesn't support mock static methods, constructors, etc. Even the website keeps saying: "Don't mock everything". So use static methods as little as possible.

It is generally recommended to provide static methods only in some utility classes, in which case you don't need mocks and just use real classes. If the dependent class is not a utility class, static methods can be refactored into instance methods. This is more in line with the object-oriented design concept.

### 6. Completeness

Test coverage, this is a very difficult problem. For the core process, we hope to achieve 90% coverage, non-core process requirements more than 60%.

High enough coverage will reduce the probability of bugs and also reduce the cost of our regression tests. This is a long process, and whenever developers add or modify code, test cases need to be refined at the same time. We hope developers and relevant code reviewer will pay enough attention to this point.

### 7. Refusion invalid assertion

Invalid assertions make the test itself meaningless, it has little to do with whether your code is correct or not. And there is a risk of creating an illusion of success that may last until your code is deploying to production.

There are several types of invalid assertions:

1. Different types of comparisons.

2. Determines that an object or variable with a default value is not null.

   This seems meaningless. Therefore, when making the relevant judgements you should pay attention to whether it contains a default value itself.

3. Assertions should be affirmative rather than negative if possible. Assertions should be within a range of predicted results, or exact values, whenever possible (otherwise you may end up with something that doesn't match your actual expectations but passes the assertion) unless your code only cares about whether it is empty or not.

### 8. Some points to note for unit tests

1: Thread.sleep()

Try not to use Thread.sleep in your test code, it makes the test unstable and may fail unexpectedly due to the environment or load. The following approach is recommended.

`Awaitility.await().atMost(...)`

2: Ignore some test classes

The @Disabled annotation should be linked to the relevant issue address so that subsequent developers can track the history of why the test was ignored.

For example @Disabled("see #1").

3: try-catch Unit test exception

The test will fail when the code in the unit test throws an exception. Therefore, there is no need to use try-catch to catch exceptions.

        ```java
        @Test
        public void testMethod() {
          try {
                    // Some code
          } catch (MyException e) {
            Assert.fail(e.getMessage());  // Noncompliant
          }
        }
        ```

You should this:

```java
@Test
public void testMethod() throws MyException {
    // Some code
}
```

4: Test exceptions

When you need to test for exceptions, you should avoid including multiple method invocations in your test code (especially if there are multiple methods that can raise the same exception), and you should clearly state what you are testing for.

5: Refuse to use MockitoJUnitRunner.Silent.class

When an UnnecessaryStubbingException occurs in a unit test, do not first consider using @RunWith(MockitoJUnitRunner.Silent.class) to resolve it. This just hides the problem, and you should follow the exception hint to resolve the issue in question, which is not a difficult task. When the changes are done, you will find that your code is much cleaner again.
