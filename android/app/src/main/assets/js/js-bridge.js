/**
 * 客户端与JS通信协议。 客户端会在加载WebView的时候，会向JS中注入jsbridge对象
 */

/**
 * 通过 Scheme url 与 app 通信
 * 直接使用 window.location.href 会坑，连续发协议可能会导致漏发
 * @param  {string} hrefstr scheme url
 */
function communicateWithAppByIframe(hrefstr) {
    var $iframe;
    $iframe = document.createElement('iframe');
    $iframe.width = 0;
    $iframe.height = 0;
    $iframe.style.display = 'none';
    setTimeout(function () {
        document.body.removeChild($iframe);
    }, 100);
    $iframe.src = hrefstr;
    document.body.appendChild($iframe);
}

var jsbrideMethodId = 0;
var JSBridgeMethodMap = {};

var JSBridge = {
	/**
	 * 获取值
	 * 
	 * @param method
	 *            需要调用的方法名
	 * @param params
	 *            参数，Map类型
	 * @param callback
	 *            回调，function(map);
	 */
	request : function(method, params, callback) {
        jsbrideMethodId = jsbrideMethodId + 1; // Must be at the first line
        var url = JSBridge.generateUrl(method, params);

		if (jsbridge) { // 客户端注入的对象
	        console.log("已经注入jsbridge了");
			var result = jsbridge.request(url);
			JSBridge.response(methodIdString, eval('(' + result + ')'))
			return;
		} else {
		    // put callback to map
		    var methodIdString = method + "_" + jsbrideMethodId;
		    JSBridgeMethodMap[methodIdString] = callback;

	    	communicateWithAppByIframe(url);
		}
	},

	/**
	 * 客户端代码，调用JS方法
	 *
	 * @param methodId
	 *            方法ID，即request时，参数jsbrideMethodId的值
	 * @param params
	 *            回调的值，Map类型
	 */
	response : function(methodId, result) {
		var callback = JSBridgeMethodMap[methodId];
		if (callback) {
			delete JSBridgeMethodMap[methodId];
			callback(result);
		}
	},

    /**
     * Get value from app directly.
     *
	 * @param method
	 *            需要调用的方法名
	 * @param params
	 *            参数，Map类型
	 *
	 * @return The result value. (string)
     */
	getValue : function(method, params) {
	    if (jsbridge) { // 客户端注入的对象
	        console.log("已经注入jsbridge了");
	        var url = JSBridge.generateUrl(method, params);
	        return jsbridge.getValue(url);
	    }
	    return undefined;
	},

	generateUrl : function(method, params){
		var methodIdString = method + "_" + jsbrideMethodId;
		var url = "jsbridge://" + method;
		// 拼接参数
		if (params) {
			for ( var key in params) {
				if (url.indexOf("?", 0) >= 0) { // url中已经有?了
					url += "&";
				} else {
					url += "?";
				}
				url += key;
				url += "=";
				url += encodeURIComponent(params[key]);
			}
		}

		if (url.indexOf("?", 0) >= 0) { // url中已经有?了
			url += "&";
		} else {
			url += "?";
		}
		return url + "jsbridgeMethodId=" + methodIdString;
	}
};

