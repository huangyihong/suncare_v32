package com.ai.modules.ybFj.constants;

/**
 * @author : zhangly
 * @date : 2023/3/3 10:06
 */
public class DcFjConstants {
    /**未归档*/
    public static final String PROJECT_STATE_INIT = "init";
    /**已归档*/
    public static final String PROJECT_STATE_FINISH = "finish";
    /**关闭*/
    public static final String PROJECT_STATE_CLOSE = "close";

    public static final String TEMPLATE_PATH = "/templates/fj/";

    public static final long LIMIT_FLOW = 20*1024*1024L;

    /**文件类型*/
    public static final String FILE_TYPE_EXCEL = "excel";
    public static final String FILE_TYPE_WORD = "word";
    public static final String FILE_TYPE_PDF = "pdf";

    public static final String FILE_EXT_XLS = "xls";
    public static final String FILE_EXT_XLSX = "xlsx";
    public static final String FILE_EXT_DOC = "doc";
    public static final String FILE_EXT_DOCX = "docx";
    public static final String FILE_EXT_PDF = "pdf";

    /**文件操作方式-上传*/
    public static final String FILE_OPER_TYPE_UP = "up";
    /**文件操作方式-输出*/
    public static final String FILE_OPER_TYPE_OUT = "out";

    /**文件所属环节、所属分类*/
    /**线索提交*/
    public static final String CLUE_STEP_SUBMIT = "submit";
    /**医院复核*/
    public static final String CLUE_STEP_HOSP = "hosp";
    /**线上核减*/
    public static final String CLUE_STEP_CUT = "cut";
    /**现场检查*/
    public static final String CLUE_STEP_ONSITE = "onsite";

    /**文件所属分类*/
    /**审核任务-提交*/
    public static final String FILE_STEP_TASK = "task";
    /**审核任务-审核反馈*/
    public static final String FILE_STEP_TASK_AUDIT = "task-audit";
    /**医院反馈任务-提交*/
    public static final String FILE_STEP_HOSP_TASK = "hosp-task";
    /**医院反馈任务-审核反馈*/
    public static final String FILE_STEP_HOSP_TASK_AUDIT = "hosp-task-audit";
    /**线索核减任务-提交*/
    public static final String FILE_STEP_CUT_TASK = "cut-task";
    /**线索核减任务-审核*/
    public static final String FILE_STEP_CUT_TASK_AUDIT = "cut-task-audit";
    /**现场检查-上传材料*/
    public static final String FILE_STEP_ONSITE_UPLOAD = "onsite-upload";

    /**待审核*/
    public static final String CLUE_STATE_INIT = "init";
    /**已通过*/
    public static final String CLUE_STATE_FINISH = "finish";
    /**不通过*/
    public static final String CLUE_STATE_FAIL = "fail";
    /**驳回*/
    public static final String CLUE_STATE_REJECT = "reject";
    /**待反馈*/
    public static final String HOSP_STATE_INIT = "init";
    /**待核减*/
    public static final String HOSP_STATE_CUT = "cut";
    /**已认可*/
    public static final String HOSP_STATE_ACCEPT = "accept";
    /**不认可*/
    public static final String HOSP_STATE_NOTACCEPT = "noaccept";
    /**待核减*/
    public static final String CUT_STATE_INIT = "init";
    /**已核减*/
    public static final String CUT_STATE_FINISH = "finish";

    /**传输类型-系统*/
    public static final String CHAT_TRANSTER_TYPE_SYS = "sys";
    /**传输类型-医院*/
    public static final String CHAT_TRANSTER_TYPE_ORG = "org";

    public static final String CHAT_TYPE_TXT = "txt";
    public static final String CHAT_TYPE_FILE = "file";

    /**已读*/
    public static final String CHAT_READ_STATE_YES = "y";
    /**未读*/
    public static final String CHAT_READ_STATE_NO = "n";

    public static final String WS_CMD_FJCHAT_SYS = "fjSys";
    public static final String WS_CMD_FJCHAT_ORG = "fjOrg";

    /**已*/
    public static final String STATE_YES = "y";
    /**未*/
    public static final String STATE_NO = "n";
}
