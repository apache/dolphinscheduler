name: Build DolphinScheduler 3.2.2 with CDH zookeeper.version=3.4.5-cdh6.3.2

on:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-22.04

    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          ref: 3.2.2-release

      - name: Setup JDK 8
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '8'
          cache: maven

      - name: Modify ZooKeeper version
        run: |
          sed -i 's/<zookeeper.version>3.4.14<\/zookeeper.version>/<zookeeper.version>3.4.5<\/zookeeper.version>/' \
            dolphinscheduler-bom/pom.xml

          grep -n "zookeeper.version" dolphinscheduler-bom/pom.xml

      - name: Build
        run: |
          ./mvnw clean install \
            -Prelease \
            -Dzk-3.4 \
            -DskipTests

      - name: Check ZooKeeper dependency
        run: |
          ./mvnw dependency:tree \
            -Dincludes=org.apache.zookeeper:zookeeper \
            -Dzk-3.4

      - name: Upload distribution
        uses: actions/upload-artifact@v4
        with:
          name: dolphinscheduler-3.2.2-zk345
          path: dolphinscheduler-dist/target/apache-dolphinscheduler-3.2.2-bin.tar.gz
