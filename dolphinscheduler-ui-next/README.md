# Dolphin Scheduler UI Next

---

> After two and a half months of development cycle, we have brought a brand-new `UI` management system (V1.0.0-Alpha).
>
> Compared with the old `UI`, it will be more standardized, and it will also have a more complete type checking mechanism. At the same time, its speed has made a qualitative leap.
>
> We also provide dark mode and light mode to meet the preferences of different developers. It will make your eyes shine.
>
> If you have tried the new `UI` and found problems in use, you can contact us through issue.

---

### Start Using

For the best experience, we recommend using `node 16.x.x` and `pnpm 6.x.x`.
You can learn how to install the corresponding version from their official website.

- [node](https://nodejs.org/en/)
- [pnpm](https://pnpm.io/)

#### Install Dependencies

```shell
pnpm install
```

#### Start Development Project

To do this you need to change the `VITE_APP_DEV_WEB_URL` parameter variable in `.env.development` in the project root directory.

It is worth noting that when you do not change the request path or route, you only need to write `http`, `ip` and `port` without the `/` symbol at the end, such as `http://127.0. 0.1:12345`.

```shell
pnpm run dev
```

#### Build Project

We provide two packaging and compilation environments by default, `development` and `production`. Their interface request configurations are in `.env.development` and `.env.production` in the project root directory respectively. Please change the `URL` is the address of the corresponding backend service.

```shell
pnpm run build:dev
# or
pnpm run build:prod
```

---
