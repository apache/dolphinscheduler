# Commit Message Notice

A good commit message states **what changed and why** at a glance. The full story belongs in the linked Issue / Pull Request — keep the commit message itself concise.

## Format

```
[Type-ISSUE_ID][Scope] Subject

(optional body — bullet list of the main changes)

(optional footer — BREAKING CHANGE / Closes #xxx)
```

Header is required; body and footer are optional. Aim for a Subject under 72 characters so it does not get truncated on GitHub.

### Type (required)

|     Type      |                                                When to use                                                |  Issue ID required?  |
|---------------|-----------------------------------------------------------------------------------------------------------|----------------------|
| `Feature`     | A new user-visible feature                                                                                | Yes                  |
| `Improvement` | Enhancement to an existing feature (refactor, perf, UX polish)                                            | Yes                  |
| `Fix`         | Bug fix                                                                                                   | Yes                  |
| `Doc`         | Documentation only                                                                                        | Yes                  |
| `DSIP`        | A change implementing a [DSIP](https://github.com/apache/dolphinscheduler/issues?q=label%3ADSIP) proposal | Yes (the DSIP issue) |
| `Chore`       | Build, CI, test scaffolding, dependency bumps, trivial cleanup                                            | No                   |

Every type **except `Chore`** must carry an Issue ID. If no Issue exists for the change, file one first or reclassify it as `Chore`.

### Scope (optional)

The module the change touches: `Master`, `Worker`, `API`, `UI`, `TaskPlugin`, `Dao`, etc. Match a real module name. Omit `[Scope]` when the change is genuinely cross-cutting (most `Chore` commits).

### Subject (required)

A short imperative sentence describing the change.

- Use the imperative mood: *Add*, *Fix*, *Remove* — not *Added* / *Adds*.
- State the **purpose**, not the implementation detail. The diff already shows the *how*.
- No trailing period.

### Body (optional, recommended for non-trivial changes)

When the change is not self-evident from the Subject, add a bullet list of the main changes. Keep it tight — one line per change point. Lengthy rationale, design notes, or testing plans go in the **Pull Request description** (see [PULL_REQUEST_TEMPLATE.md](https://github.com/apache/dolphinscheduler/blob/dev/.github/PULL_REQUEST_TEMPLATE.md)), not in the commit message.

### Footer (optional)

- `BREAKING CHANGE: <description>` — for incompatible changes. Required if the change breaks RPC, DB schema, or public API compatibility. Also add an entry to `docs/docs/en/guide/upgrade/incompatible.md`.
- `Closes #1234` — to auto-close the linked issue on merge.

## Examples

Good:

```
[Fix-18201][TaskPlugin] Fix RemoteShell task NullPointerException on empty stdout
```

```
[Improvement-18224][API] Migrate EnvironmentService to typed return values

- Replace Map<String, Object> returns with dedicated DTOs
- Update controller and unit tests accordingly
```

```
[Chore][API] Remove deprecated ProjectService#checkProjectAndAuth
```

```
[Feature-17900][Master] Support task-group priority override at runtime

- Add priority field to TaskGroupQueue
- Honor override when dispatching ready tasks

Closes #17900
```

Avoid:

- `fix bug` — no type prefix, no scope, no information.
- `[fix] update master` — wrong casing, missing issue ID, vague subject.
- `[Improvement][Master] refactor scheduler to use new state machine and also fix a NPE in worker dispatch and adjust some logs` — multiple unrelated changes; split into separate commits.

## References

- [PULL_REQUEST_TEMPLATE.md](https://github.com/apache/dolphinscheduler/blob/dev/.github/PULL_REQUEST_TEMPLATE.md)
- [Pull Request Notice](./pull-request.md)
- [Apache Geode commit message format](https://cwiki.apache.org/confluence/display/GEODE/Commit+Message+Format)
- [On commit messages — Peter Hutterer](http://who-t.blogspot.com/2009/12/on-commit-messages.html)

