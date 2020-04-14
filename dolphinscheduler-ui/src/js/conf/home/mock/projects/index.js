import Mock from 'mockjs';
import { resultDTO } from '@/conf/home/mock';

// Get workFlowList
const getWorkFlowList = () => {
  return {
    ...resultDTO,
    data: [
      {
        "workFlowId": "7",
        "workFlowName": "数据采集_mysql2hdfs_ods_participant_account_business_agent"
      },
      {
        "workFlowId": "9",
        "workFlowName": "数据采集_mysql2hdfs_ods_participant_mdm_ps_person_erp_rela_bi"
      },
      {
        "workFlowId": "14",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
      {
        "workFlowId": "15",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
      {
        "workFlowId": "16",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
      {
        "workFlowId": "17",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
      {
        "workFlowId": "18",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
      {
        "workFlowId": "19",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
      {
        "workFlowId": "20",
        "workFlowName": "数据采集_sybase2hdfs_ods_production_erp_spxx"
      },
    ]
  }
}

// Get workFlow DAG
const getWorkFlowDAG = () => {
  return {
    ...resultDTO,
    data: {
      "workFlowList": [
        {
          "workFlowId": "7",
          "workFlowName": "数据清洗_dwd_trade_product_decuct_daily",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "0 0 8 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "9",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_ztsptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "54 54 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "14",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "15",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "16",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "17",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "18",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "19",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        },
        {
          "workFlowId": "20",
          "workFlowName": "数据采集_sybase2hdfs_ods_trade_erp_sptcitemr",
          "workFlowPublishStatus": "1",
          "scheduleStartTime": "2019-11-23 00:00:00",
          "scheduleEndTime": "2022-12-31 00:00:00",
          "crontab": "31 28 2 * * ? *",
          "schedulePublishStatus": "1"
        }
      ],
      "workFlowRelationList": [
        {
          "sourceWorkFlowId": "7",
          "targetWorkFlowId": "9"
        },
        {
          "sourceWorkFlowId": "7",
          "targetWorkFlowId": "14"
        },
      ]
    }
  }
}

// the key of api should be splitted by ' '
const api = {
  '/dolphinscheduler/lineages/list-name get': getWorkFlowList,
  '/dolphinscheduler/lineages/list-ids get': getWorkFlowDAG,
};

export default api;
