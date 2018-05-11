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
    <a href="javascript:;" download="tentacle.png" class="x-cmd x-cmd-right x-cmd-printscreen" title="截屏另存为"></a>
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
<div class="x-dialog x-dialog-keyboard">
    <div class="x-title">虚拟键盘<i class="x-close"></i></div>
    <hr />
    <div class="x-keyboard">
        <div class="x-funcs">
            <b class="x-2b">ESC</b>
            <i></i>
            <b>F1</b>
            <b>F2</b>
            <b>F3</b>
            <b>F4</b>
            <i></i>
            <b>F5</b>
            <b>F6</b>
            <b>F7</b>
            <b>F8</b>
            <i></i>
            <b>F9</b>
            <b>F10</b>
            <b>F11</b>
            <b>F12</b>
            <div class="clearfix"></div>
        </div>
        <div>
            <div class="x-kb-main">
                <div>
                    <b class="x-shiftkey">`<sup>~</sup></b>
                    <i></i>
                    <b class="x-shiftkey">1<sup>!</sup></b>
                    <b class="x-shiftkey">2<sup>@</sup></b>
                    <b class="x-shiftkey">3<sup>#</sup></b>
                    <b class="x-shiftkey">4<sup>$</sup></b>
                    <b class="x-shiftkey">5<sup>%</sup></b>
                    <b class="x-shiftkey">6<sup>^</sup></b>
                    <b class="x-shiftkey">7<sup>&</sup></b>
                    <b class="x-shiftkey">8<sup>*</sup></b>
                    <b class="x-shiftkey">9<sup>(</sup></b>
                    <b class="x-shiftkey">0<sup>)</sup></b>
                    <b class="x-shiftkey">-<sup>_</sup></b>
                    <b class="x-shiftkey">=<sup>+</sup></b>
                    <b class="x-2b x-align-right">BACK</b>
                    <div class="clearfix"></div>
                </div>
                <div>
                    <b class="x-2b x-align-left">TAB</b>
                    <i></i>
                    <b>Q</b>
                    <b>W</b>
                    <b>E</b>
                    <b>R</b>
                    <b>T</b>
                    <b>Y</b>
                    <b>U</b>
                    <b>I</b>
                    <b>O</b>
                    <b>P</b>
                    <b class="x-shiftkey">[<sup>{</sup></b>
                    <b class="x-shiftkey">]<sup>}</sup></b>
                    <b class="x-shiftkey">\<sup>|</sup></b>
                    <div class="clearfix"></div>
                </div>
                <div>
                    <b class="x-2b x-align-left">CAPS</b>
                    <i></i>
                    <b>A</b>
                    <b>S</b>
                    <b>D</b>
                    <b>F</b>
                    <b>G</b>
                    <b>H</b>
                    <b>J</b>
                    <b>K</b>
                    <b>L</b>
                    <b class="x-pressed">;<sup>:</sup></b>
                    <b>'<sup>"</sup></b>
                    <b class="x-3b x-align-right">ENTER</b>
                </div>
                <div>
                    <b class="x-4b x-align-left">SHIFT</b>
                    <i></i>
                    <b>Z</b>
                    <b>X</b>
                    <b>C</b>
                    <b>V</b>
                    <b>B</b>
                    <b>N</b>
                    <b>M</b>
                    <b>,<sup>&lt;</sup></b>
                    <b>.<sup>&gt;</sup></b>
                    <b>/<sup>?</sup></b>
                    <b class="x-4b x-align-right">SHIFT</b>
                </div>
                <div>
                    <b class="x-2b x-align-left">CTRL</b>
                    <i></i>
                    <b class="x-2b">ALT</b>
                    <i></i>
                    <b class="x-spacebar">SPACE</b>
                    <i></i>
                    <b class="x-2b">ALT</b>
                    <i></i>
                    <b class="x-2b x-align-right">CTRL</b>
                    <div class="clearfix"></div>
                </div>
            </div>
            <div class="x-kb-ctrl">
                <div>
                    <b class="x-2b">INS</b>
                    <b class="x-2b">HOME</b>
                    <b class="x-2b">PGUP</b>
                    <div class="clearfix"></div>
                </div>
                <div>
                    <b class="x-2b">DEL</b>
                    <b class="x-2b">END</b>
                    <b class="x-2b">PGDN</b>
                    <div class="clearfix"></div>
                </div>
                <div class="x-dir">
                    <s></s>
                    <b>&blacktriangle;</b>
                    <s></s>
                    <div class="clearfix"></div>
                    <b>&blacktriangleleft;</b>
                    <b>&blacktriangledown;</b>
                    <b>&blacktriangleright;</b>
                    <div class="clearfix"></div>
                </div>
            </div>
            <div class="clearfix"></div>
        </div>
    </div>
    <div class="x-actions">
        <button class="btn">CTRL+ALT+DELETE</button>
        <button class="btn">发送组合键</button>
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