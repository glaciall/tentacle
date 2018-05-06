<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tentacle Desktop</title>
    <link rel="stylesheet" type="text/css" href="${web_resource}/tentacle.css" />
    <link rel="stylesheet" type="text/css" href="http://apps.bdimg.com/libs/animate.css/3.1.0/animate.min.css" />
</head>
<body>
<div class="x-screen">
    <canvas id="screen" width="800" height="600"></canvas>
</div>
<div class="x-cmd-bar">
    <div class="x-cmd x-cmd-left x-cmd-color" title="颜色模式"></div>
    <div class="x-cmd x-cmd-left x-cmd-copy" title="文本复制与粘贴"></div>
    <div class="x-cmd x-cmd-left x-cmd-keyboard" title="发送组合键"></div>
    <div class="x-icon"></div>
    <div class="x-cmd x-cmd-right x-cmd-disconnect" title="断开连接"></div>
    <div class="x-cmd x-cmd-right x-cmd-printscreen" title="截屏"></div>
    <div class="x-cmd x-cmd-right x-cmd-transfer" title="文件传送"></div>
</div>
<div class="x-stat-panel">
    <div class="x-stat" id="x-frames">
        <h1>0</h1>
        <h5>frames</h5>
    </div>
    <hr />
    <div class="x-stat" id="x-last-frame">
        <h1>0 kb</h1>
        <h5>last frame</h5>
    </div>
    <hr />
    <div class="x-stat" id="x-bytes">
        <h1>0 kb</h1>
        <h5>total transfer</h5>
    </div>
    <hr />
</div>
<div class="x-auth-dialog">
    <div class="x-title">输入密码开始连接</div>
    <hr />
    <div class="x-password"><input id="password" type="text" /></div>
    <div class="x-message"></div>
    <div class="x-button">
        <button id="btn-auth">开始连接</button>
    </div>
</div>
<div class="x-dialog x-dialog-clipboard">
    <div class="x-title">文本复制与粘贴<i class="x-close"></i></div>
    <hr />
    <div>远程剪切板</div>
    <textarea id="clipboard-remote" readonly></textarea>
    <div>本地剪切板</div>
    <textarea id="clipboard-local" placeholder="请输入或粘贴文本到此"></textarea>
    <div class="x-actions">
        <button class="btn" id="btn-send">发送到远程主机</button>
    </div>
</div>
<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>

<script type="text/javascript">
    $.fn.extend({
        animateCss: function(animationName, callback) {
            var animationEnd = (function(el) {
                var animations = {
                    animation: 'animationend',
                    OAnimation: 'oAnimationEnd',
                    MozAnimation: 'mozAnimationEnd',
                    WebkitAnimation: 'webkitAnimationEnd',
                };

                for (var t in animations) {
                    if (el.style[t] !== undefined) {
                        return animations[t];
                    }
                }
            })(document.createElement('div'));

            this.addClass('animated ' + animationName).one(animationEnd, function() {
                $(this).removeClass('animated ' + animationName);

                if (typeof callback === 'function') callback();
            });

            return this;
        },
    });
</script>
<script type="text/javascript" src="${web_resource}/decompress.js"></script>
<script type="text/javascript" src="${web_resource}/tentacle.js"></script>
<script type="text/javascript">
    $(document).ready(function()
    {
        Tentacle.init();
    });
</script>
</body>
</html>