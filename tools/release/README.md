# Tools Release

A tools for convenient release DolphinScheduler.

## Prepare

* python: python 3.6 or above
* pip: latest version of pip is better

To install dependence, you should run command

```shell
python -m pip install -r requirements.txt
```

## Usage

### Export Environment Variable

You can create new token in [create token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token),
it is only need all permission under `repo`

```shell
export GH_ACCESS_TOKEN="<YOUR-GITHUB-TOKEN-WITH-REPO-ACCESS>"
export GH_REPO_MILESTONE="<YOUR-MILESTONE>"
```

### Help

```shell
python release.py -h
```

### Action

* Auto cherry-pick: `python release.py cherry-pick`, will cause error when your default branch is not up-to-date, or cherry-pick with conflict. But if you fix you can directly re-run this command, it will continue the pick
* Generate changelog: `python release.py changelog`
* Generate contributor: `python release.py contributor`
