<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tentacle Desktop</title>
    <style type="text/css">
        #message-box
        {
            width: 400px;
            height: 30px;
            line-height: 30px;
            font-size: 14px;
            display: block;
        }
        canvas
        {
            border: solid 2px #f1f1f1;
            display: block;
        }
    </style>
</head>
<body>
<canvas width="1920" height="1080" id="screen"></canvas>
<input type="text" id="message-box" placeholder="输入内容后回车" />
<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
<script type="text/javascript" src="${web_resource}/decompress.js"></script>
<script type="text/javascript">
    var canvas = document.getElementById('screen').getContext('2d');
    var imageData = canvas.createImageData(1920, 1080);
    $(function()
    {
        var ws = new WebSocket('ws://localhost:8888/tentacle/desktop/wss');
        ws.binaryType = "arraybuffer";

        ws.onopen = function()
        {
            console.log('websocket opened...');
        };

        ws.onmessage = function(resp)
        {
            if (!(resp.data instanceof ArrayBuffer)) return console.error('server response: ' + resp.data);
            decompress('rle', new Uint8Array(resp.data), imageData);
            canvas.putImageData(imageData, 0, 0);
        }

        ws.onclose = function()
        {
            console.log('websocket closed...');
        }

        ws.onerror = function()
        {
            console.log('websocket error', arguments);
        }

        $('#message-box').keyup(function(e)
        {
            if (e.keyCode == 10 || e.keyCode == 13)
            {
                ws.send($(this).val());
                $(this).val('');
            }
        });

        var index = 0;
        setInterval(function()
        {
            ws.send(index);
            index++;
            if (index > 145) index = 0;
        }, 60);
    });

</script>
</body>
</html>