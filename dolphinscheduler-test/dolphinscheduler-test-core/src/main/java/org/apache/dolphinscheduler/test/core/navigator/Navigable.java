package org.apache.dolphinscheduler.test.core.navigator;

import org.apache.dolphinscheduler.test.core.Module;
import org.openqa.selenium.WebElement;

import java.util.List;

public interface Navigable {
    Navigator find();

    Navigator $();

    Navigator find(int index);

    Navigator find(List<Integer> range);

    Navigator $(int index);

    Navigator $(List<Integer> range);

    Navigator $(Navigator[] navigators);

    Navigator $(WebElement[] elements);

    Navigator focused();


    public <T extends Module> T module(Class<T> moduleClass);


    public <T extends Module> T module(T module);
}
