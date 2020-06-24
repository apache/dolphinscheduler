package org.apache.dolphinscheduler.spi.params;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * radio
 */
public class RadioParam extends AbsPluginParams {

    public RadioParam(String name, String label, List<String> optionValues, String defaultValue, boolean isRequired, boolean readOnly) {
        super(name);
        requireNonNull(label , "label is null");
        requireNonNull(optionValues , "optionValues is null");
        if(optionValues.size() == 0) {
            throw new RuntimeException("optionValues is empty");
        }

        this.alpacajsSchema = new AlpacajsSchema();
        this.alpacajsSchema.setTitle(label);
        this.alpacajsSchema.setDefaultValue(defaultValue);
        this.alpacajsSchema.setType(DataType.STRING.getDataType());
        this.alpacajsSchema.setRequired(isRequired);
        this.alpacajsSchema.setEnumValues(optionValues);

        this.alpacajsOptions = new AlpacajsOptions();
        this.alpacajsOptions.setType(FormType.RADIO.getFormType());
        this.alpacajsOptions.setReadOnly(readOnly);
    }

    public List<String> getValues() {
        return this.alpacajsSchema.getEnumValues();
    }

    public void setValues(List<String> values) {
        this.alpacajsSchema.setEnumValues(values);
    }

    /**
     * add one value
     * @param value
     *  value
     * @return
     */
    public RadioParam addValue(String value) {
        if (this.alpacajsSchema.getEnumValues() == null) {
            this.alpacajsSchema.setEnumValues(new ArrayList<>());
        }
        this.alpacajsSchema.getEnumValues().add(value);
        return this;
    }
}
