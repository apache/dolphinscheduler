# Code of Conduct

The following Code of Conduct is based on full compliance with the [Apache Software Foundation Code of Conduct](https://www.apache.org/foundation/policies/conduct.html).

## Development philosophy

- **Consistent** code style, naming, and usage are consistent.
- **Easy to read** code is obvious, easy to read and understand, when debugging one knows the intent of the code.
- **Neat** agree with the concepts of《Refactoring》and《Code Cleanliness》and pursue clean and elegant code.
- **Abstract** hierarchy is clear and the concepts are refined and reasonable. Keep methods, classes, packages, and modules at the same level of abstraction.
- **Heart** Maintain a sense of responsibility and continue to be carved in the spirit of artisans.

## Development specifications

- Executing `mvn -U clean package -Prelease` can compile and test through all test cases.
- The test coverage tool checks for no less than dev branch coverage.
- In the root directory, use Checkstyle to check your code for special reasons for violating validation rules. The template location is located at ds_check_style.xml.
- Follow the coding specifications.

## Coding specifications

- Use linux line breaks.
- Indentation (including empty lines) is consistent with the last line.
- An empty line is required between the class declaration and the following variable or method.
- There should be no meaningless empty lines.
- Classes, methods, and variables should be named as the name implies and abbreviations should be avoided.
- Return value variables are named after `result`; `each` is used in loops to name loop variables; and `entry` is used in map instead of `each`.
- The cached exception is called `e`; Catch the exception and do nothing, and the exception is named `ignored`.
- Configuration Files are named in camelCase, and file names are lowercase with uppercase initial/starting letter.
- Code that requires comment interpretation should be as small as possible and interpreted by method name.
- `equals` and `==` In a conditional expression, the constant is left, the variable is on the right, and in the expression greater than less than condition, the variable is left and the constant is right.
- In addition to the abstract classes used for inheritance, try to design the class as `final`.
- Nested loops are as much a method as possible.
- The order in which member variables are defined and the order in which parameters are passed is consistent across classes and methods.
- Priority is given to the use of guard statements.
- Classes and methods have minimal access control.
- The private method used by the method should follow the method, and if there are multiple private methods, the writing private method should appear in the same order as the private method in the original method.
- Method entry and return values are not allowed to be `null`.
- The return and assignment statements of if else are preferred with the tri-objective operator.
- Priority is given to `LinkedList` and only use `ArrayList` if you need to get element values in the collection through the index.
- Collection types such as `ArrayList`，`HashMap` that may produce expansion must specify the initial size of the collection to avoid expansion.
- Logs and notes are always in English.
- Comments can only contain `javadoc`, `todo` and `fixme`.
- Exposed classes and methods must have javadoc, other classes and methods and methods that override the parent class do not require javadoc.

## Unit test specifications

- Test code and production code are subject to the same code specifications.
- Unit tests are subject to AIR (Automatic, Independent, Repeatable) Design concept.
  - Automatic: Unit tests should be fully automated, not interactive. Manual checking of output results is prohibited, `System.out`, `log`, etc. are not allowed, and must be verified with assertions.
  - Independent: It is prohibited to call each other between unit test cases and to rely on the order of execution. Each unit test can be run independently.
  - Repeatable: Unit tests cannot be affected by the external environment and can be repeated.
- Unit tests are subject to BCDE（Border, Correct, Design, Error) Design principles.
  - Border (Boundary value test): The expected results are obtained by entering the boundaries of loop boundaries, special values, data order, etc.
  - Correct (Correctness test): The expected results are obtained with the correct input.
  - Design (Rationality Design): Design high-quality unit tests in combination with production code design.
  - Error (Fault tolerance test): The expected results are obtained through incorrect input such as illegal data, abnormal flow, etc.
- If there is no special reason, the test needs to be fully covered.
- Each test case needs to be accurately asserted.
- Prepare the environment for code separation from the test code.
- Only jUnit `Assert`，hamcrest `CoreMatchers`，Mockito Correlation can use static import.
- Single-data assertions should use `assertTrue`，`assertFalse`，`assertNull` and `assertNotNull`.
- Multi-data assertions should use `assertThat`.
- Accurate assertion, try not to use `not`，`containsString` assertion.
- The true value of the test case should be named actualXXX, and the expected value should be named expectedXXX.
- Classes and Methods with `@Test` labels do not require javadoc.
- Public specifications.
  - Each line is no longer than `200` in length, ensuring that each line is semantically complete for easy understanding.

