export const shell = {
  locales: {
    zh_CN: {
      node_name: '节点名称',
      node_name_tips: '节点名称不能为空'
    },
    en_US: {
      node_name: 'Node Name',
      node_name_tips: 'Node name cannot be empty'
    }
  },
  items: [
    {
      label: 'node_name',
      type: 'input',
      field: '',
      validate: {
        trigger: ['input', 'blur'],
        message: 'node_name_tips'
      }
    }
  ]
}