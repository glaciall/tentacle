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
        this.connect();
        this.showLoginDialog();
        this._startTimer();

        this.canvas = document.getElementById('screen').getContext('2d');
        this.imageData = this.canvas.createImageData(10, 10);
    },
    // 身份验证
    login : function()
    {
        var password = $('#password').val();
        if ($.trim(password).length == 0) return $('.x-message').html('请输入密码');
        this._send({
            type : 'command',
            command : 'request-control',
            password : password
        });
        $('#btn-auth').addClass('disable');
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
        if (this.state != 'controlling') return setTimeout(function(){ self.__decompressAndShow(); }, 50);
        var compressedData = this.frames.shift();
        if (compressedData == undefined) return setTimeout(function(){ self.__decompressAndShow(); }, 50);

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
        var self = this;
        if (this.connection && this.connection.readyState == 1) return;
        this.frames = [];
        this.hidCommands = [];
        this.state = 'standby';
        this.connection = new WebSocket('ws://' + location.host + '/tentacle/desktop/wss');
        this.connection.onopen = function()
        {
            self._onopen.apply(self, arguments);
        };
        this.connection.onmessage = function()
        {
            self._onmessage.apply(self, arguments);
        };
        this.connection.onclose = function()
        {
            self._onclose.apply(self, arguments);
        };
        this.connection.onerror = function()
        {
            self._onerror.apply(self, arguments);
        };
        this.connection.binaryType = 'arraybuffer';
    },
    _onopen : function() { this.state = 'connected'; },
    _onmessage : function(resp)
    {
        var self = this;
        if (resp.data instanceof ArrayBuffer)
        {
            var packet = new Uint8Array(resp.data);
            this.frames.push(packet);
        }
        else
        {
            var response = eval('(' + resp.data + ')');
            console.log('action: ' + response.action);
            if ('login' == response.action)
            {
                $('#btn-auth').removeClass('disable');
                if (response.result == 'success')
                {
                    $('.x-auth-dialog').animateCss('bounceOut', function() { $('.x-auth-dialog').hide(); });
                    self.showMessage('登陆成功');
                }
                else $('.x-message').text(response.result);
            }
            else if ('request-control' == response.action)
            {
                if (response.result != 'success') this.showMessage(response.result);
            }
            else if ('setup' == response.action)
            {
                this.state = 'controlling';
            }
            else if ('read-clipboard' == response.action)
            {

            }
            else if ('write-clipboard' == response.action)
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
        screenElement.onmousewheel = function(e)
        {
            if (self.state != 'controlling') return;
            // 1 向上，2向下
            self.__addHIDEvent({
                type : 'mouse-wheel',
                key : e.deltaY < 0 ? 1 : 2,
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
            if (self.state != 'controlling') return;
            self.__addHIDEvent({
                type : 'mouse-move',
                x : e.offsetX,
                y : e.offsetY,
                timestamp : parseInt(e.timeStamp)
            });
            if (e.preventDefault) e.preventDefault();
            return false;
        }
        window.onkeydown = function(e)
        {
            if (self.state != 'controlling') return;
            self.__addHIDEvent({
                type : 'key-press',
                key : e.keyCode,
                timestamp : parseInt(e.timeStamp)
            });
            if (e.preventDefault) e.preventDefault();
            return false;
        }
        window.onkeyup = function(e)
        {
            if (self.state != 'controlling') return;
            self.__addHIDEvent({
                type : 'key-release',
                key : e.keyCode,
                timestamp : parseInt(e.timeStamp)
            });
            if (e.preventDefault) e.preventDefault();
            return false;
        }
        // 释放所有按键
        window.onblur = function(e)
        {
            for (var i = 0; i < self.keyboard.length; i++)
            {
                if (self.keyboard[i]) ;
            }
        }
        $('#btn-auth').click(function()
        {
            self.login();
        });
    },
    __addHIDEvent : function(cmd)
    {
        this.hidCommands.push(cmd);
    },
    __sendHIDCommands : function()
    {
        var self = this;
        if (this.hidCommands.length == 0) return setTimeout(function(){ self.__sendHIDCommands(); }, 50);
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
        this._send({
            type : 'command',
            command : 'get-clipboard',
        });
    },
    // 设置远程主机的剪切板内容
    setRemoteClipboard : function(text)
    {
        this._send({
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