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
            <b class="x-2b" x-key="27">ESC</b>
            <i></i>
            <b class="" x-key="112">F1</b>
            <b class="" x-key="113">F2</b>
            <b class="" x-key="114">F3</b>
            <b class="" x-key="115">F4</b>
            <i></i>
            <b class="" x-key="116">F5</b>
            <b class="" x-key="117">F6</b>
            <b class="" x-key="118">F7</b>
            <b class="" x-key="119">F8</b>
            <i></i>
            <b class="" x-key="120">F9</b>
            <b class="" x-key="121">F10</b>
            <b class="" x-key="122">F11</b>
            <b class="" x-key="123">F12</b>
            <div class="clearfix"></div>
        </div>
        <div>
            <div class="x-kb-main">
                <div>
                    <b class="x-shiftkey" x-key="192">`<sup>~</sup></b>
                    <i></i>
                    <b class="x-shiftkey" x-key="49">1<sup>!</sup></b>
                    <b class="x-shiftkey" x-key="50">2<sup>@</sup></b>
                    <b class="x-shiftkey" x-key="51">3<sup>#</sup></b>
                    <b class="x-shiftkey" x-key="52">4<sup>$</sup></b>
                    <b class="x-shiftkey" x-key="53">5<sup>%</sup></b>
                    <b class="x-shiftkey" x-key="54">6<sup>^</sup></b>
                    <b class="x-shiftkey" x-key="55">7<sup>&amp;</sup></b>
                    <b class="x-shiftkey" x-key="56">8<sup>*</sup></b>
                    <b class="x-shiftkey" x-key="57">9<sup>(</sup></b>
                    <b class="x-shiftkey" x-key="48">0<sup>)</sup></b>
                    <b class="x-shiftkey" x-key="189">-<sup>_</sup></b>
                    <b class="x-shiftkey" x-key="187">=<sup>+</sup></b>
                    <b class="x-2b x-align-right" x-key="8">BACK</b>
                    <div class="clearfix"></div>
                </div>
                <div>
                    <b class="x-2b x-align-left" x-key="9">TAB</b>
                    <i></i>
                    <b class="" x-key="81">Q</b>
                    <b class="" x-key="87">W</b>
                    <b class="" x-key="69">E</b>
                    <b class="" x-key="82">R</b>
                    <b class="" x-key="84">T</b>
                    <b class="" x-key="89">Y</b>
                    <b class="" x-key="85">U</b>
                    <b class="" x-key="73">I</b>
                    <b class="" x-key="79">O</b>
                    <b class="" x-key="80">P</b>
                    <b class="x-shiftkey" x-key="219">[<sup>{</sup></b>
                    <b class="x-shiftkey" x-key="221">]<sup>}</sup></b>
                    <b class="x-shiftkey" x-key="220">\<sup>|</sup></b>
                    <div class="clearfix"></div>
                </div>
                <div>
                    <b class="x-2b x-align-left" x-key="20">CAPS</b>
                    <i></i>
                    <b class="" x-key="65">A</b>
                    <b class="" x-key="83">S</b>
                    <b class="" x-key="68">D</b>
                    <b class="" x-key="70">F</b>
                    <b class="" x-key="71">G</b>
                    <b class="" x-key="72">H</b>
                    <b class="" x-key="74">J</b>
                    <b class="" x-key="75">K</b>
                    <b class="" x-key="76">L</b>
                    <b class="" x-key="186">;<sup>:</sup></b>
                    <b class="" x-key="222">'<sup>"</sup></b>
                    <b class="x-3b x-align-right" x-key="13">ENTER</b>
                </div>
                <div>
                    <b class="x-4b x-align-left" x-key="16">SHIFT</b>
                    <i></i>
                    <b class="" x-key="90">Z</b>
                    <b class="" x-key="88">X</b>
                    <b class="" x-key="67">C</b>
                    <b class="" x-key="86">V</b>
                    <b class="" x-key="66">B</b>
                    <b class="" x-key="78">N</b>
                    <b class="" x-key="77">M</b>
                    <b class="" x-key="188">,<sup>&lt;</sup></b>
                    <b class="" x-key="190">.<sup>&gt;</sup></b>
                    <b class="" x-key="191">/<sup>?</sup></b>
                    <b class="x-4b x-align-right" x-key="16">SHIFT</b>
                </div>
                <div>
                    <b class="x-2b x-align-left" x-key="17">CTRL</b>
                    <i></i>
                    <b class="x-2b" x-key="18">ALT</b>
                    <i></i>
                    <b class="x-spacebar" x-key="32">SPACE</b>
                    <i></i>
                    <b class="x-2b" x-key="18">ALT</b>
                    <i></i>
                    <b class="x-2b x-align-right" x-key="17">CTRL</b>
                    <div class="clearfix"></div>
                </div>
            </div>
            <div class="x-kb-ctrl">
                <div>
                    <b class="x-2b" x-key="45">INS</b>
                    <b class="x-2b" x-key="36">HOME</b>
                    <b class="x-2b" x-key="33">PGUP</b>
                    <div class="clearfix"></div>
                </div>
                <div>
                    <b class="x-2b" x-key="46">DEL</b>
                    <b class="x-2b" x-key="35">END</b>
                    <b class="x-2b" x-key="34">PGDN</b>
                    <div class="clearfix"></div>
                </div>
                <div class="x-dir">
                    <s></s>
                    <b class="" x-key="38">▴</b>
                    <s></s>
                    <div class="clearfix"></div>
                    <b class="" x-key="37">◂</b>
                    <b class="" x-key="40">▾</b>
                    <b class="" x-key="39">▸</b>
                    <div class="clearfix"></div>
                </div>
            </div>
            <div class="clearfix"></div>
        </div>
    </div>
    <div class="x-actions">
        <button class="btn" id="btn-send-keys">发送组合键</button>
    </div>
</div>
<div class="x-dialog x-dialog-fmanager">
    <div class="x-title">远程主机文件管理<i class="x-close"></i></div>
    <hr />
    <div class="x-fmanager">
        <div class="x-path"><a href="#">/</a><a href="#">opt/</a><a href="#">software/</a></div>
        <table cellpadding="4" cellspacing="0" border="1" width="100%">
            <thead>
            <tr>
                <th align="left">文件名</th>
                <th width="20%" align="center">类型</th>
                <th width="20%" align="right">大小</th>
                <th width="10%" align="center">-</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
    <div class="x-actions">
        <button class="btn" id="btn-upload">上传本地文件</button>
    </div>
</div>
<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
<script type="text/javascript" src="${web_resource}/fileext.js"></script>
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