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
    <canvas id="screen"></canvas>
</div>
<div class="x-cmd-bar">
    <div class="x-icon"></div>
</div>
<div class="x-stat-panel">
    <div class="x-stat" id="x-frames">
        <h1>1234</h1>
        <h5>frames</h5>
    </div>
    <hr />
    <div class="x-stat" id="x-bytes">
        <h1>12.77 mb</h1>
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
<script type="text/javascript">
    var ws = null;
    var canvas = document.getElementById('screen').getContext('2d');
    var imageData = canvas.createImageData(10, 10);
    var authenticated = false;
    var remoteControlling = false;
    var frames = [];
    var totalTransfered = 0;

    $(function()
    {
        $('.x-auth-dialog').animateCss('bounceIn');
        $('.x-stat-panel').animateCss('slideInLeft');
        $('.x-cmd-bar').animateCss('slideInDown');

        ws = new WebSocket('ws://' + location.host + '/tentacle/desktop/wss');
        ws.binaryType = "arraybuffer";

        ws.onopen = function()
        {
            console.log('websocket opened...');
        };

        ws.onmessage = function(resp)
        {
            if (!(resp.data instanceof ArrayBuffer)) return console.error('server response: ' + resp.data);
            remoteControlling = true;
            frames.push(new Uint8Array(resp.data));
        }

        ws.onclose = function()
        {
            console.log('websocket closed...');
        }

        ws.onerror = function()
        {
            console.log('websocket error', arguments);
        }

        $('#btn-auth').click(function(e)
        {
            var password = $('#password').val();
            if ($.trim(password).length == 0) return alert('请输入密码进行连接');
            ws.send('{ \"type\" : "command", \"command\" : \"request-control\", \"password\" : \"' + password + '\" }');
            $('.x-auth-dialog').animateCss('bounceOut', function()
            {
                $('.x-auth-dialog').hide();
            });
        });

        var mousePressing = false;
        $('#screen').mousedown(function(e)
        {
            if (!remoteControlling) return;
            mousePressing = true;
            // 1 左键，2 中键，3 右键
            var key = e.which;
            hidCommands.push({
                type : 'mouse-down',
                key : key,
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
        });

        $('#screen').mouseup(function(e)
        {
            if (!remoteControlling) return;
            mousePressing = false;
            var key = e.which;
            hidCommands.push({
                type : 'mouse-up',
                key : key,
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
            e.preventDefault();
            return false;
        });

        $('#screen').contextmenu(function(e)
        {
            e.preventDefault();
            return false;
        });

        var lastMousePositionCaptured = 0;
        $('#screen').mousemove(function(e)
        {
            if (!remoteControlling) return;
            var now = new Date().getTime();
            if (now - lastMousePositionCaptured < 50) return;
            hidCommands.push({
                type : 'mouse-move',
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
            lastMousePositionCaptured = now;
            e.preventDefault();
            return false;
        });

        var screenElement = $('#screen').get(0);
        screenElement.onmousewheel = function(e)
        {
            if (!remoteControlling) return;
            // 1 向上，2向下
            var key = 0;
            hidCommands.push({
                type : 'mouse-wheel',
                key : e.deltaY < 0 ? 1 : 2,
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
        }

        window.onkeydown = function(e)
        {
            if (!remoteControlling) return;
            hidCommands.push({
                type : 'key-press',
                key : e.keyCode,
                timestamp : parseInt(e.timeStamp)
            });
        }
        window.onkeyup = function(e)
        {
            if (!remoteControlling) return;
            hidCommands.push({
                type : 'key-release',
                key : e.keyCode,
                timestamp : parseInt(e.timeStamp)
            });
        }
    });

    var hidCommands = [];

    // 每30毫秒发送一次
    // 鼠标：坐标与键位、动作
    // 键盘：键位与动作
    setInterval(function()
    {
        if (hidCommands.length == 0) return;
        var packet = { type : "hid", commands : [] };
        for (var i = 0, l = hidCommands.length; i < l; i++)
        {
            packet.commands.push(hidCommands.shift());
        }
        ws.send(JSON.stringify(packet));
    }, 30);

    function f()
    {
        if (!remoteControlling) return setTimeout(f, 50);
        var compressedData = frames.shift();
        if (compressedData == undefined) return setTimeout(f, 50);

        var width = ((compressedData[0] << 8) | compressedData[1]) & 0xffff;
        var height = ((compressedData[2] << 8) | compressedData[3]) & 0xffff;
        var x = '';
        for (var i = 4; i < 12; i++)
        {
            x = x + ('00' + compressedData[i].toString(16)).replace(/^0+(\w{2})$/gi, '$1');
        }
        var captureTime = parseInt(x, 16);
        var sequence = (compressedData[12] << 24 | compressedData[13] << 16 | compressedData[14] << 8 | compressedData[15]) & 0xffffffff;
        totalTransfered += (compressedData.length / 1024);
        var tf = totalTransfered.toFixed(2) + ' kb';
        if (totalTransfered > 1024) tf = (totalTransfered / 1024).toFixed(2) + ' mb';
        $('#x-frames h1').html(sequence);
        $('#x-bytes h1').html(tf);
        if (imageData.width != width || imageData.height != height)
        {
            var screenElement = document.getElementById('screen');
            screenElement.width = width;
            screenElement.style.width = width;
            screenElement.height = height;
            screenElement.style.height = height;
            canvas.width = width;
            canvas.height = height;
            screenElement.style.marginLeft = parseInt(0 - width / 2) + 'px';
            screenElement.style.marginTop = parseInt(0 - height / 2) + 'px';
            imageData = canvas.createImageData(width, height);
        }
        decompress('rle', compressedData, imageData);
        canvas.putImageData(imageData, 0, 0);
        setTimeout(f, 50);
    }

    setTimeout(f, 50);

</script>
</body>
</html>