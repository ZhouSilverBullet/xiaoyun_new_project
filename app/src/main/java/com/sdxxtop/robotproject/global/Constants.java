package com.sdxxtop.robotproject.global;

/**
 * @author Orion
 * @time 2018/9/11
 */
public class Constants {

    // fragment tag
    public static final String FRAGMENT_FUNC_LIST = "fragment_func_list";
    public static final String FRAGMENT_SPEECH = "fragment_speech";
    public static final String FRAGMENT_NAVIGATION = "fragment_navigation";
    public static final String FRAGMENT_MAP = "fragment_map";
    public static final String FRAGMENT_MOVE = "fragment_move";
    public static final String FRAGMENT_FACE = "fragment_face";
    // requestId
    public static final int REQUEST_ID_DEFAULT = 0;

    // message key
    public static final String BUNDLE_ID = "bundle_id";
    public static final String BUNDLE_REQUEST_TYPE = "bundle_request_type";
    public static final String BUNDLE_REQUEST_TEXT = "bundle_request_text";
    public static final String BUNDLE_REQUEST_PARAM = "bundle_request_param";
    public static final String BUNDLE_CMD_COMMAND = "bundle_cmd_command";
    public static final String BUNDLE_CMD_STATUS = "bundle_cmd_status";

    // callback intent
    public static final String REQUEST_TYPE_SPEECH = "req_speech_wakeup";
    public static final String REQUEST_TYPE_CRUISE = "robot_navigation&cruise";
    public static final String REQUEST_TYPE_GUIDE = "guide&guide";
    public static final String REQUEST_TYPE_SET_LOCATION = "robot_navigation&set_location";
    public static final String REQUEST_TYPE_ASK = "register&ask";
    public static final String REQUEST_TYPE_REGISTER = "register&register";

    // callback type
    /**
     * 语音唤醒
     * New request:  type is:req_speech_wakeup text is:null reqParam = 2
     */
    public static final String REQ_SPEECH_WAKEUP = "req_speech_wakeup";

    //
    public static final int MSG_SPEECH = 110;

    // cons value
    public static final long START_NAVIGATION_TIME_OUT = 10 * 1000;
    public static final double COORDINATE_DEVIATION = 0.5;

}
