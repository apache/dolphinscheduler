package org.apache.dolphinscheduler.spi.params;

import static java.util.Objects.requireNonNull;

/**
 *Text param
 */
public class PasswordParam extends AbsPluginParams{


    public PasswordParam(String name, String label, String defaultValue, String placeholder, boolean isRequired, boolean readOnly) {
        super(name);

        requireNonNull(label , "label is null");

        alpacajsSchema = new AlpacajsSchema();
        alpacajsSchema.setTitle(label);
        alpacajsSchema.setDefaultValue(defaultValue);
        alpacajsSchema.setType(DataType.STRING.getDataType());
        alpacajsSchema.setRequired(isRequired);

        alpacajsOptions = new AlpacajsOptions();
        alpacajsOptions.setPlaceholder(placeholder);
        alpacajsOptions.setType(FormType.PASSWORD.getFormType());
        alpacajsOptions.setReadOnly(readOnly);
    }
}
