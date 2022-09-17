# Documentation Notice

Good documentation is critical for any type of software. Any contribution that can improve the DolphinScheduler documentation is welcome.

### Get the document project

Documentation for the DolphinScheduler project is maintained in a separate [git repository](https://github.com/apache/dolphinscheduler-website).

First you need to fork the document project into your own github repository, and then clone the document to your local computer.

```
git clone https://github.com/<your-github-user-name>/dolphinscheduler-website
```

### The document environment

The DolphinScheduler website is supported by [docsite](https://github.com/chengshiwen/docsite-ext)

Make sure that your node version is 10+, docsite does not yet support versions higher than 10.x.

### Document build guide

1. Run `npm install` in the root directory to install the dependencies.

2. Run commands to collect resources 2.1.Run `export PROTOCOL_MODE=ssh` tells Git clone resource via SSH protocol instead of HTTPS protocol. 2.2.Run `./scripts/prepare_docs.sh` prepare all related resources, for more information you could see [how prepare script work](https://github.com/apache/dolphinscheduler-website/blob/master/HOW_PREPARE_WOKR.md).

3. Run `npm run start` in the root directory to start a local server, you will see the website in 'http://localhost:8080'.

4. Run `npm run build` to build source code into dist directory.

5. Verify your change locally: `python -m SimpleHTTPServer 8000`, when your python version is 3 use :`python3 -m http.server 8000` instead.

If the latest version of node is installed locally, consider using `nvm` to allow different versions of `node` to run on your computer.

1. Refer to the [Instructions](http://nvm.sh) to install nvm.

2. Run `nvm install v10.23.1` to install node v10.

3. Run `nvm use v10.23.1` to switch the current working environment to node v10.

Now you can run and build the website in your local environment.

### The document specification

1. ** Spaces are Required ** between Chinese characters and English or numbers and ** Spaces are not required ** between Chinese punctuation marks and English or numbers, to enhance the aesthetics and readability of the Chinese-English mix.

2. It is recommended that you use "you" in general. Of course, you can use the term when necessary, such as when there is a warning prompt.

### How to submit a document Pull Request

1. Do not use "git add." to commit all changes.

2. Simply push the changed files, for example:

* `*.md`
* `blog.js or docs.js or site.js`

3. Submit the Pull Request to the **master** branch.

### Reference to the documentation

[Apache Flink Translation Specifications](https://cwiki.apache.org/confluence/display/FLINK/Flink+Translation+Specifications)
