/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


console.log("Backend working");

/**
 * @type {HttpHeader[]} headers
 * @type {string} header
 * @return {void}
 */
function setHeader(headers, header, value) {
    for (let i = 0; i < headers.length; i++) {
        let h = headers[i];
        if (h.name == header) {
            h.value = value
            return
        }
    }
    headers.push({
        name: header,
        value: value
    })
}

chrome.webRequest.onBeforeSendHeaders.addListener((details) => {
        let headers = details.requestHeaders;
        setHeader(headers, "X-Requested-With", "com.tencent.tim");
        return {requestHeaders: headers};
    },
    {urls: ["*://ssl.captcha.qq.com/**"]},
    ["requestHeaders", "blocking"]
);
