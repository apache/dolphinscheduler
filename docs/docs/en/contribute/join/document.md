# Documentation Notice

Good documentation is critical for any type of software. Any contribution that can improve the DolphinScheduler documentation is welcome.

### Get the document project

Documentation for the DolphinScheduler project is maintained in a separate [git repository](https://github.com/apache/dolphinscheduler-website).

First you need to fork the document project into your own github repository, and then clone the document to your local computer.

```
git clone https://github.com/<your-github-user-name>/dolphinscheduler-website
```

### Document build guide

1. Run `yarn` in the root directory to install the dependencies.
2. Run commands to collect resources
   2.1. Run `export PROTOCOL_MODE=ssh` tells Git clone resource via SSH protocol instead of HTTPS protocol
   2.2. Run `./scripts/prepare_docs.sh` prepare all related resources, for more information you could see [how prepare script work](https://github.com/apache/dolphinscheduler-website/blob/master/HOW_PREPARE_WORK.md)
3. Run `yarn generate` in the root directory to format and prepare the data.
4. Run `yarn dev` in the root directory to start a local server, you will see the website in 'http://localhost:3000'.

```
Note: if you clone the code in Windows, not Mac or Linux. Please read the details below.
If you execute the commands like the two steps above, you will get the exception "UnhandledPromiseRejectionWarning: Error: EPERM: operation not permitted, symlink '2.0.3' -> 'latest'".
If you get the exception "Can't resolve 'antd' in xxx",you can run `yarn add antd` and `yarn install`.
Because the `./scripts/prepare_docs.sh` command requires a Linux environment, if you are on a Windows system, you can use WSL to complete this step.
When you encounter this problem. You can run the two steps in cmd.exe as an administrator on your Windows system.
```

5. Run `yarn build` to build source code, this will automatically generate a directory called `build`, wait for the execution to complete and into `build` directory.
6. Verify your change locally: `python -m SimpleHTTPServer 8000`, when your python version is 3 use :`python3 -m http.server 8000` instead.

If you have higher version of node installed, you may consider `nvm` to allow different versions of `node` coexisting on your machine.

1. Follow the [instructions](http://nvm.sh) to install nvm
2. Run `nvm install v18.12.1` to install node v18
3. Run `nvm use v18.12.1` to switch the working environment to node v18

Then you are all set to run and build the website. Follow the build instruction above for the details.

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
