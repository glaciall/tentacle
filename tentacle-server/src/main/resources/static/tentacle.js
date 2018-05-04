window.Tentacle = {
    imageData : null,
    canvas : null,
    totalTransfered : 0,
    keyboard : [],
    mouse : [],
    hidCommands : [],
    connection : null,
    // 状态：standby,connected,controlling,disconnected
    state : 'standby',
    frames : [],

    // 初始化
    init : function()
    {
        if (this.connection != null) return;
        this._bindEvents();
        this._connect();
        this.showLoginDialog();
        this._startTimer();

        this.canvas = document.getElementById('screen').getContext('2d');
        this.imageData = this.canvas.createImageData(10, 10);
    },
    // 身份验证
    login : function()
    {
        var password = $('#password').val();
        if ($.trim(password).length == 0) return this.showMessage('请输入密码进行连接');
        this.send({
            type : 'command',
            command : 'request-control',
            password : password
        });
    },
    // 定时器
    _startTimer : function()
    {
        var self = this;
        setTimeout(function()
        {
            self.__decompressAndShow();
        }, 50);
        setTimeout(function()
        {
            self.__sendHIDCommands();
        }, 50);
    },
    // 图像解压并显示
    __decompressAndShow : function()
    {
        var self = this;
        if (this.state != '') return setTimeout(this.__decompressAndShow, 50);
        var compressedData = this.frames.shift();
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
        this.totalTransfered += (compressedData.length / 1024);
        var tf = this.totalTransfered.toFixed(2) + ' kb';
        if (this.totalTransfered > 1024) tf = (this.totalTransfered / 1024).toFixed(2) + ' mb';
        $('#x-frames h1').html(sequence);
        $('#x-bytes h1').html(tf);
        if (this.imageData.width != width || this.imageData.height != height)
        {
            var screenElement = document.getElementById('screen');
            screenElement.width = width;
            screenElement.style.width = width;
            screenElement.height = height;
            screenElement.style.height = height;
            this.canvas.width = width;
            this.canvas.height = height;
            screenElement.style.marginLeft = parseInt(0 - width / 2) + 'px';
            screenElement.style.marginTop = parseInt(0 - height / 2) + 'px';
            this.imageData = this.canvas.createImageData(width, height);
        }
        decompress('rle', compressedData, this.imageData);
        this.canvas.putImageData(this.imageData, 0, 0);
        setTimeout(function()
        {
            self.__decompressAndShow();
        }, 50);
    },

    // /////////////////////////////////////////////////////////////////////
    // 断开连接
    disconnect : function()
    {
        this.connection.close();
    },
    _send : function(cmd)
    {
        this.connection.send(JSON.stringify(cmd));
    },
    connect : function()
    {
        if (this.connection && this.connection.readyState == 1) return;
        this.frames = [];
        this.hidCommands = [];
        this.state = 'standby';
        this.connection = new WebSocket('ws://' + location.host + '/tentacle/desktop/ws');
        this.connection.onopen = this._onopen;
        this.connection.onmessage = this._onmessage;
        this.connection.onclose = this._onclose;
        this.connection.onerror = this._onerror;
        this.connection.binaryType = 'arraybuffer';
    },
    _onopen : function() { this.state = 'connected'; },
    _onmessage : function(resp)
    {
        if (resp.data instanceof ArrayBuffer)
        {
            var packet = new Uint8Array(resp.data);
            this.state = 'connected';
            this.frames.push(packet);
        }
        else
        {
            var response = eval(resp.data);
            console.log(response);
            if ('login' == resp.action)
            {
                $('#btn-auth').removeClass('disable');
                if (resp.result == 'success') $('.x-auth-dialog').animateCss('bounceOut');
                else $('.x-message').text(resp.result);
            }
            else if ('request-control' == resp.action)
            {
                if (resp.result != 'success') this.showMessage(resp.result);
            }
            else if ('setup' == resp.action)
            {
                this.state = 'controlling';
            }
            else if ('read-clipboard' == resp.action)
            {

            }
            else if ('write-clipboard' == resp.action)
            {

            }
        }
    },
    _onclose : function() { this.state = 'disconnected'; },
    _onerror : function() { },


    // /////////////////////////////////////////////////////////////////////
    // 事件绑定
    _bindEvents : function()
    {
        var self = this;
        var screenElement = document.getElementById('screen');
        // 鼠标按下
        screenElement.onmousedown = function(e)
        {
            if (self.state != 'controlling') return;
            // 1 左键，2 中键，3 右键
            var key = e.which;
            self.__addHIDEvent({
                type : 'mouse-down',
                key : key,
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
        }
        screenElement.onmouseup = function(e)
        {
            if (self.state != 'controlling') return;
            var key = e.which;
            self.__addHIDEvent({
                type : 'mouse-up',
                key : key,
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
            if (e.preventDefault) e.preventDefault();
            return false;
        }
        screenElement.oncontextmenu = function(e)
        {
            if (e.preventDefault) e.preventDefault();
            return false;
        }
        screenElement.onmousemove = function(e)
        {
            self.__addHIDEvent({
                type : 'mouse-move',
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
            if (e.preventDefault) e.preventDefault();
            return false;
        }
        // 释放所有按键
        window.onblur = function(e)
        {
            for (var i = 0; i < this.keyboard.length; i++)
            {
                if (this.keyboard[i]) ;
            }
        }
    },
    __addHIDEvent : function(cmd)
    {
        this.hidCommands.push(cmd);
    },
    __sendHIDCommands : function()
    {
        var self = this;
        if (this.hidCommands.length == 0) return;
        var packet = { type : "hid", commands : [] };
        for (var i = 0, l = this.hidCommands.length; i < l; i++)
        {
            packet.commands.push(this.hidCommands.shift());
        }
        this._send(packet);
        setTimeout(function()
        {
            self.__sendHIDCommands();
        }, 50);
    },

    // /////////////////////////////////////////////////////////////////////
    // 获取远程主机的剪切板内容
    getRemoteClipboard : function()
    {
        this.send({
            type : 'command',
            command : 'get-clipboard',
        });
    },
    // 设置远程主机的剪切板内容
    setRemoteClipboard : function(text)
    {
        this.send({
            type : 'command',
            command : 'set-clipboard',
            text : text
        });
    },


    // /////////////////////////////////////////////////////////////////////
    // UI相关
    showLoginDialog : function()
    {
        $('.x-auth-dialog').animateCss('bounceIn');
    },
    showMessage : function(text)
    {
        var timeout = 4000;
        var box = $('<div class="x-message-box">' + text + '</div>');
        $(document.body).append(box);
        box.css({ top : (document.body.scrollTop + window.innerHeight - 100) + 'px' }).show().animateCss('bounceIn', function()
        {
            setTimeout(function()
            {
                box.remove();
            }, timeout);
        });
    },
};