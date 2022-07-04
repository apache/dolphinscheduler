# DolphinScheduler Test

DolphinScheduler Test module contains api + ui test cases and test execution framework to ensure more elegant writing
and running of test cases

# Test module structure

- test-case:
  - cases (API + UI test cases)
    - Scenario test
    - Single function test
  - endpoint (API testing building blocks)
  - pages (UI testing building blocks
- test-core:
  - exception
  - extensions
  - js
  - navigator
  - url
  - webstorage
  - Browser
  - Module
  - Page
  - PageEventListener

# Test Example

see ```org/apache/dolphinscheduler/test/cases```

# Test Type

1. Validation Testing: It forms one of the last segments in the software development process. It caters to the testing
   explicitly done to assess the product; its behavior, and its efficiency. The API is examined for correctness in
   implementation as per the expectations. Additionally, the API is also verified based on pre-established agreed-upon
   criteria such as the delivery of specific end goals, integration with the specified environment. In addition to that,
   the testing of API's behavior to access the correct data happens as a part of Validation testing.
2. Functional Testing: This is one of the broader types of testing. It aims to test specified functions. While testing
   the APIs for functional testing, the assessment of the responses received happens against expected responses in terms
   of passing parameters. Additionally, the error handling for unsuccessful requests and invalid responses too are under
   consideration under these tests. The boundary cases, along with regular tests, fall under the scope of these tests.
3. UI Testing: UI Tests tend to be more specific. These tests assess the user interface against the APIs and their
   constituent parts. Additionally, the tests are more generalized with a focus on the API health; it's usability as
   well as compatibility of the front end and back end.
4. Load Testing: This test is for the functionality and performance of the APIs under load. The API undergoes
   theoretical regular traffic. The test results form the baseline to conduct further load testing. Additionally, the
   testing also includes subjecting the API to maximum possible traffic to test the behavior of the API under full
   loads. APIs undergo overloading tests. In this test, the verification of the API performance and error handling
   conditions during failure happens.
5. Error Detection: The tests for APIs, which include the monitoring, inducing execution errors, sending invalid
   requests, detecting operational leaks, etc. fall under this category. The introduction of known failure scenarios in
   the APIs happens. The testing of these APIs happens to ensure that the errors are detected, handled as well as
   routed.
6. API Security tests: Specific tests verify the APIs performance for vulnerabilities from external threats. Security
   testing, Penetration testing, and Fuzz testing are some of them. For example, Security testing involves validation of
   the APIs in terms of security requirements. Additionally, these security requirements could be related to
   permissions, authorizations, authentications, etc. An authorized attack launches against the system as a part of
   Penetration testing. It evaluates the security of the API. A full-scale assessment report of strengths and weaknesses
   of the APIs is the deliverable issued after such a test. In Fuzz testing, evaluation of API behavior happens. It is
   subjected to unexpected, invalid, and random data of enormous size. Following this, the crashes, built-in assertions,
   and memory leaks are known.
7. Integration testing: The Integration tests focus on API communication with another module APIs.
8. Reliability testing: The API should display a prompt response for different configurations. It also looks for a
   response data structure as a part of testing.

# FAQ

## Page object models

### Overview

Within your web app’s UI there are areas that your tests interact with. A Page Object simply models these as objects
within the test code. This reduces the amount of duplicated code and means that if the UI changes, the fix need only be
applied in one place.

Page Object is a Design Pattern which has become popular in test automation for enhancing test maintenance and reducing
code duplication. A page object is an object-oriented class that serves as an interface to a page of your AUT. The tests
then use the methods of this page object class whenever they need to interact with the UI of that page. The benefit is
that if the UI changes for the page, the tests themselves don’t need to change, only the code within the page object
needs to change. Subsequently all changes to support that new UI are located in one place.

### Advantages

- There is a clean separation between test code and page specific code such as locators (or their use if you’re using a
  UI Map) and layout.
- There is a single repository for the services or operations offered by the page rather than having these services
  scattered throughout the tests. In both cases this allows any modifications required due to UI changes to all be made
  in one place. Useful information on this technique can be found on numerous blogs as this ‘test design pattern’ is
  becoming widely used. We encourage the reader who wishes to know more to search the internet for blogs on this
  subject. Many have written on this design pattern and can provide useful tips beyond the scope of this user guide. To
  get you started, though, we’ll illustrate page objects with a simple example.

