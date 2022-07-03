package org.apache.dolphinscheduler.test.core.url;

public class UrlFragment {

    private final String fragment;

    private UrlFragment(String fragment) {
        this.fragment = fragment;
    }

    static UrlFragment of(String fragment) {
        return new UrlFragment(fragment);
    }

    @Override
    public String toString() {
        return fragment;
    }
}
