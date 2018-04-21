<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tentacle Desktop</title>
    <style type="text/css">
        #btn-request-control
        {
            height: 30px;
            font-size: 14px;
            width: 100px;
        }
        .x-screen
        {
            box-sizing: content-box !important;
            width: 1280px;
            height: 720px;
            position: relative;
            border: solid 2px #cccccc;
        }
        .x-screen canvas
        {
            display: block;
        }
        .x-screen .x-info
        {
            width: 300px;
            height: 30px;
            border-bottom-left-radius: 4px;
            border-bottom-right-radius: 4px;
            background-color: #f0ad4e;
            position: absolute;
            top: 0px;
            left: 50%;
            margin-left: -150px;
            z-index: 100;
            text-align: center;
        }
        .x-screen .x-info .x-frame
        {
            float: left;
            width: 30%;
            height: 30px;
            line-height: 30px;
        }
        .x-screen .x-info .x-bytes
        {
            float: left;
            width: 70%;
            height: 30px;
            line-height: 30px;
        }
        body
        {
            margin: 0px;
            padding: 0px;
        }
    </style>
</head>
<body>
<div class="x-screen">
    <canvas width="1280" height="720" id="screen"></canvas>
    <div class="x-info">
        <div class="x-frame">sequence</div>
        <div class="x-bytes">package size</div>
    </div>
</div>

<input type="button" id="btn-request-control" value="请求控制" />
<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
<script type="text/javascript" src="${web_resource}/decompress.js"></script>
<script type="text/javascript">
    var ws = null;
    var canvas = document.getElementById('screen').getContext('2d');
    var imageData = canvas.createImageData(1280, 720);
    $(function()
    {
        ws = new WebSocket('ws://localhost:8888/tentacle/desktop/wss');
        ws.binaryType = "arraybuffer";

        ws.onopen = function()
        {
            console.log('websocket opened...');
        };

        ws.onmessage = function(resp)
        {
            if (!(resp.data instanceof ArrayBuffer)) return console.error('server response: ' + resp.data);
            var time = new Date().getTime();
            decompress('rle', new Uint8Array(resp.data), imageData);
            canvas.putImageData(imageData, 0, 0);
            time = new Date().getTime() - time;
            if (time > 10) console.log('spend: ' + time);
        }

        ws.onclose = function()
        {
            console.log('websocket closed...');
        }

        ws.onerror = function()
        {
            console.log('websocket error', arguments);
        }

        $('#btn-request-control').click(function(e)
        {
            ws.send('{ \"type\" : "command", \"command\" : \"request-control\" }');
        });

        var mousePressing = false;
        $('#screen').mousedown(function(e)
        {
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
            mousePressing = false;
            var key = e.which;
            hidCommands.push({
                type : 'mouse-up',
                key : key,
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
        });

        var lastMousePositionCaptured = 0;
        $('#screen').mousemove(function(e)
        {
            var now = new Date().getTime();
            if (mousePressing || (now - lastMousePositionCaptured < 100)) return;
            hidCommands.push({
                type : 'mouse-move',
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
            lastMousePositionCaptured = now;
        });
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

</script>
</body>
</html>