import {h} from "vue";
import {NTag} from "naive-ui";


export function renderEnvironmentalDistinctionCell(
    testFlag: number | undefined,
    t: Function
) {
    if (testFlag === 0) {
        return h(
            NTag,
            { type: 'success', size: 'small' },
            {
                default: () => t('datasource.on_line')
            }
        )
    } else if (testFlag === 1) {
        return h(
            NTag,
            { type: 'warning', size: 'small' },
            {
                default: () => t('datasource.test')
            }
        )
    } else {
        return '-'
    }
}

