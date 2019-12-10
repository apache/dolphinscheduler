* First from the remote repository *https://github.com/apache/incubator-dolphinscheduler.git* fork code to your own repository

* there are three branches in the remote repository currently:
     * master normal delivery branch
            After the stable version is released, the code for the stable version branch is merged into the master branch.

    * dev daily development branch
            The daily development branch, the newly submitted code can pull requests to this branch.


* Clone your own warehouse to your local

    `git clone https://github.com/apache/incubator-dolphinscheduler.git`

* Add remote repository address, named upstream

   `git remote add upstream https://github.com/apache/incubator-dolphinscheduler.git`

* View repository:

    `git remote -v`

> There will be two repositories at this time: origin (your own warehouse) and upstream (remote repository)

* Get/update remote repository code (already the latest code, skip it)

   `git fetch upstream`


* Synchronize remote repository code to local repository

```
git checkout origin/dev
git merge --no-ff upstream/dev
```

If remote branch has a new branch `DEV-1.0`, you need to synchronize this branch to the local repository.

```
git checkout -b dev-1.0 upstream/dev-1.0
git push --set-upstream origin dev1.0
```

* After modifying the code locally, submit it to your own repository:

`git commit -m 'test commit'`
`git push`

* Submit changes to the remote repository

* On the github page, click on the new pull request.
<p align = "center">
<img src = "http://geek.analysys.cn/static/upload/221/2019-04-02/90f3abbf-70ef-4334-b8d6-9014c9cf4c7f.png"width ="60%"/>
</ p>

* Select the modified local branch and the branch to merge past to create a pull request.
<p align = "center">
<img src = "http://geek.analysys.cn/static/upload/221/2019-04-02/fe7eecfe-2720-4736-951b-b3387cf1ae41.png"width ="60%"/>
</ p>

*  Next, the administrator is responsible for **merging** to complete the pull request











