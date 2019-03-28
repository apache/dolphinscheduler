GitBook plugin: Insert logo link
================================

NPM package [here](https://www.npmjs.com/package/gitbook-plugin-insert-logo-link)

```
npm i gitbook-plugin-insert-logo-link
```

The following plugin inserts a logo link into the navigation bar (above the summary and above the search input). Simply, drop a `logo.png` file into the root folder of your GitBook and add this plugin into your `book.json`:

```json
{
    "plugins": ["insert-logo-link", "another plugin 1", "another plugin 2"]
}
```

You will also need to provide src for the logo and the href for the link url. The src can be local file, a remote URL, or base64 hash. Add the src and url into the plugin configuration in your `book.json`:

```json
{
    "plugins": ["insert-logo-link", "another plugin 1", "another plugin 2"],
    "pluginsConfig": {
        "insert-logo-link": {
            "src": "/my-logo.png",
            url: "http://example"
        }
    }
}
```
