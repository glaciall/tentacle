window.Tentacle = {
    imageData : null,
    canvas : null,
    totalTransfered : 0,
    keyboard : [],
    mouse : [],
    hidCommands : [],
    connection : null,
    // 状态：standby,connected,controlling,exchanging,disconnected
    state : 'standby',
    remoteDesktopSessionId : null,
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
            command : 'login',
            password : password
        });
        $('#btn-auth').addClass('disable');
    },
    // 查询主机会话列表
    querySessions : function()
    {
        this._send({ type : 'command', command : 'sessions' });
    },
    // 请求控制
    requestControl : function(sessionId)
    {
        this._send({ type : 'command', command : 'request-control', sessionId : sessionId });
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
        setTimeout(function()
        {
            self.__keepalive();
        }, 5000);
    },
    // 图像解压并显示
    __decompressAndShow : function()
    {
        var self = this;
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
        $('#x-last-frame h1').html((compressedData.length / 1024).toFixed(2) + ' kb');
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
        this._standby();
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
    _onopen : function() { this._connected(); },
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
                    $('.x-message').text('密码校验通过');
                    $('.x-auth-dialog').animateCss('bounceOut', function() { $('.x-auth-dialog').hide(); });
                    this.querySessions();
                }
                else $('.x-message').text(response.result);
            }
            else if ('sessions' == response.action)
            {
                // remoteDesktopSessionId
                var shtml = '';
                for (var i = 0; response.sessions && i < response.sessions.length; i++)
                {
                    var session = response.sessions[i];
                    shtml += '<div class="x-row">';
                    shtml += '  <div class="x-col-1 text-center">' + session.id + '</div>';
                    shtml += '  <div class="x-col-4"><a href="javascript:;" x-session-id="' + (session.controlling ? '' : session.id) + '" id="rds-name">' + session.name + '</a></div>';
                    shtml += '  <div class="x-col-4">' + session.address + '</div>';
                    shtml += '  <div class="x-col-1 text-center ' + (session.controlling ? 'orange' : 'green') + '">' + (session.controlling ? '忙碌' : '空闲') + '</div>';
                    shtml += '  <div class="x-clearfix"></div>';
                    shtml += '</div>';
                }
                $('.x-sessions').html(shtml);
                var dialog = $('.x-dialog-sessions');
                if (dialog.is(':visible') == false) $('.x-dialog-sessions').show().animateCss('bounceIn');
            }
            else if ('request-control' == response.action)
            {
                if (response.result != 'success') this.showMessage(response.result);
                $('.x-cmd-bar').animate({ top : '-70px' }, 300);
                $('.x-stat-panel').animate({ left : '-140px' }, 500, function() { $(this).find('.x-trigger').removeClass('x-trigger-left').addClass('x-trigger-right'); });
            }
            else if ('setup' == response.action)
            {
                this._controlling();
            }
            else if ('get-clipboard' == response.action)
            {
                $('#clipboard-remote').val(response.result);
            }
            else if ('set-clipboard' == response.action)
            {
                self.showMessage('己成功发送到远程主机的剪切板');
            }
            else if ('ls' == response.action)
            {
                self._showFiles(response);
            }
            else if ('upload' == response.action)
            {
                // TODO: 显示上传进度
                self.__uploadPartial(response.blockOffset, response.blockLength);
            }
        }
    },
    _onclose : function() { this._disconnected(); },
    _onerror : function() { },

    // 状态变更
    _exchanging : function()
    {
        this.state = 'exchanging';
    },
    _controlling : function()
    {
        this.state = 'controlling';
    },
    _connected : function()
    {
        this.state = 'connected';
    },
    _standby : function()
    {
        this.state = 'standby';
    },
    _disconnected : function()
    {
        this.state = 'disconnected';
    },
    _isControlling : function()
    {
        return this.state == 'controlling';
    },

    // /////////////////////////////////////////////////////////////////////
    // 事件绑定
    _bindEvents : function()
    {
        var self = this;
        var screenElement = document.getElementById('screen');
        // 鼠标按下
        screenElement.onmousedown = function(e)
        {
            if (!self._isControlling()) return;
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
            if (!self._isControlling()) return;
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
            if (!self._isControlling()) return;
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
            if (!self._isControlling()) return;
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
            if (!self._isControlling()) return;
            self.keyboard[e.keyCode] = true;
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
            if (!self._isControlling()) return;
            self.keyboard[e.keyCode] = false;
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
                if (self.keyboard[i]) self.__addHIDEvent({
                    type : 'key-release',
                    key : i,
                    timestamp : parseInt(e.timeStamp)
                });
            }
        }
        $('#btn-auth').click(function()
        {
            self.login();
        });
        $('#password').keyup(function(e)
        {
            if (e.keyCode == 10 || e.keyCode == 13)
            {
                self.login();
            }
        });
        $('.x-command-dialog .x-close').click(function()
        {
            var dialog = null;
            (dialog = $(this).parents('.x-dialog')).animateCss('bounceOut', function(){ dialog.hide(); });
            self._controlling();
        });
        // 剪切板数据交换
        $('#clipboard-remote, #clipboard-local').click(function()
        {
            $(this).select();
        });
        $('.x-cmd-copy').click(function()
        {
            if (!self._isControlling()) return;
            $('.x-dialog-clipboard').show().animateCss('bounceIn');
            $('#clipboard-local').val('');
            self.getRemoteClipboard();
            self._exchanging();
        });
        $('.x-dialog-clipboard button[id=btn-send]').click(function()
        {
            var text = $('#clipboard-local').val();
            if (text.length == 0) return self.showMessage('请输入或粘贴文件到上面的文本输入框内');
            self._send({
                type : 'command',
                command : 'set-clipboard',
                text : text
            });
        });
        // 重新获取会话列表
        $('.x-dialog-sessions .x-refresh').click(function()
        {
            $(this).animateCss('rotateIn');
            self.querySessions();
        });
        $(document).on('click', '.x-sessions a[id=rds-name]', function()
        {
            var link = $(this);
            var sid = link.attr('x-session-id');
            if (!sid) return;
            self.remoteDesktopSessionId = sid;
            self.requestControl(sid);
            // TODO: 显示等待提示
            var dialog = $('.x-dialog-sessions');
            dialog.animateCss('bounceOut', function() { dialog.hide(); });
        });
        // 下载屏幕画面
        $('.x-cmd-printscreen').click(function()
        {
            if (!self._isControlling()) return;
            var link = $(this);
            link.attr('href', screenElement.toDataURL('image/png'));
        });
        // 虚拟键盘/发送组合键
        var keys = [];
        $('.x-cmd-keyboard').click(function()
        {
            if (!self._isControlling()) return;
            $('.x-dialog-keyboard').show().animateCss('bounceIn');
            self._exchanging();
            $('.x-keyboard b').each(function()
            {
                $(this).removeClass('x-pressed');
            });
            keys = [];
        });
        $('.x-keyboard b').click(function()
        {
            var btn = $(this);
            var key = btn.attr('x-key');
            if (btn.hasClass('x-pressed'))
            {
                var newKeyList = [];
                for (var i = 0; i < keys.length; i++)
                {
                    if (keys[i] != key) newKeyList.push(keys[i]);
                }
                keys = newKeyList;
                btn.removeClass('x-pressed');
            }
            else
            {
                keys.push(key);
                btn.addClass('x-pressed');
            }
        });
        $('#btn-send-keys').click(function()
        {
            if (keys.length == 0) return self.showMessage('请在上面虚拟键盘上依次按下你要发送的按键');
            for (var i = 0; i < keys.length; i++)
            {
                self.__addHIDEvent({
                    type : 'key-press',
                    key : keys[i],
                    timestamp : 0
                });
            }
            for (var i = 0; i < keys.length; i++)
            {
                self.__addHIDEvent({
                    type : 'key-release',
                    key : keys[i],
                    timestamp : 0
                });
            }
            keys = [];
            $('.x-keyboard b').each(function()
            {
                $(this).removeClass('x-pressed');
            });
        });
        // 文件管理
        var currentPath = "";
        $('.x-cmd-transfer').click(function()
        {
            if (!self._isControlling()) return;
            $('.x-dialog-fmanager').show().animateCss('bounceIn');
            showPath(currentPath = "");
        });
        $(document).on('click', '.x-fmanager .x-filelist .x-dir', function()
        {
            var dir = unescape($(this).attr('x-name'));
            var seperator = '\\';
            if (dir.indexOf('/') > -1 || currentPath.indexOf('/') > -1) seperator = '/';
            if (dir.charAt(dir.length - 1) != seperator) dir += seperator;
            showPath(currentPath += dir);
        });
        $(document).on('click', '.x-fmanager .x-path a', function()
        {
            var dir = unescape($(this).attr('x-dir'));
            showPath(currentPath = dir);
        });
        $(document).on('click', '.x-fmanager a[id=x-download-file]', function()
        {
            var fname = $(this).attr('x-fname');
            $(this).attr('href', ROOT_PATH + '/download?rdsId=' + self.remoteDesktopSessionId + '&path=' + encodeURIComponent(currentPath) + '&name=' + fname);
        });
        function showPath(dir)
        {
            self._exchanging();
            self._send({
                type : 'command',
                command : 'ls',
                path : currentPath
            });
            var path = '<a href="javascript:;" x-dir=""><img src="' + RES_PATH + '/icon/pc.png" width="16" height="16" /></a>';
            var part = '';
            var xpath = '';
            for (var i = 0; i < dir.length; i++)
            {
                var chr = dir.charAt(i);
                if (chr == '/' || chr == '\\')
                {
                    xpath += part + chr;
                    path = path + '<a href="javascript:;" x-dir="' + escape(xpath) + '">' + part + chr + '</a>';
                    part = '';
                }
                else part = part + chr;
            }
            $('.x-fmanager .x-path').html(path);
        }

        var cmdBarTimeout = null;
        $('.x-cmd-bar .x-icon').click(function()
        {
            var bar = $(this).parents('.x-cmd-bar');
            var top = 0;
            if (bar.offset().top == -10) top = -70;
            if (bar.offset().top == -70) top = -10;
            if (top == 0) return;
            bar.animate({ top : top + 'px' }, 300);
        });

        $('.x-cmd-bar').mouseleave(function()
        {
            var bar = $(this);
            if (cmdBarTimeout) window.clearTimeout(cmdBarTimeout);
            cmdBarTimeout = setTimeout(function()
            {
                bar.animate({ top : '-70px' }, 300);
            }, 2000);
        });

        $('.x-stat-panel .x-trigger').click(function()
        {
            var trigger = $(this);
            var visible = trigger.hasClass('x-trigger-left');
            var left = visible ? '-140px' : '-10px';
            trigger.parents('.x-stat-panel').animate({ left : left }, 500, function()
            {
                trigger.removeClass('x-trigger-' + (left == '-10px' ? 'right' : 'left')).addClass('x-trigger-' + (left == '-10px' ? 'left' : 'right'));
            });
        });

        var progressCanvas = document.getElementById('x-upload-progress').getContext('2d');
        $('#btn-upload input[type=file]').fileupload({
            dataType : 'json',
            add : function(e, data)
            {
                if (data.originalFiles[0].size > MAX_UPLOAD_SIZE)
                    return warning('你选择的文件已经超过了系统设定的最大文件大小。');

                $('.x-progress').show().find('div').text('正在上传：' + data.originalFiles[0].name);
                data.submit();
            },
            formData : function(form)
            {
                return [
                    { name : 'fileId', value : HTTP_SESSION_ID + '-' + new Date().getTime() },
                    { name : 'filePath', value : currentPath },
                    { name : 'sessionId', value : self.remoteDesktopSessionId }
                ];
            },
            progressall : function(e, data)
            {
                var percent = data.loaded / data.total;
                var text = parseInt(percent * 100) + '%';

                if (data.loaded >= data.total)
                {
                    $('.x-progress').hide();
                    return;
                }
                var angle = parseInt(percent * 360);

                progressCanvas.clearRect(0, 0, 300, 300);
                progressCanvas.lineWidth = 20;
                progressCanvas.strokeStyle = '#333333';
                progressCanvas.beginPath();
                progressCanvas.arc(150, 150, 100, 0, Math.PI * 2, true);
                progressCanvas.stroke();
                progressCanvas.closePath();

                progressCanvas.strokeStyle = '#0099ff';
                progressCanvas.lineWidth = 20;
                progressCanvas.lineCap = 'round';
                progressCanvas.beginPath();
                if (angle > 0) progressCanvas.arc(150, 150, 100, angle * (Math.PI / 180), Math.PI * 2, true);
                progressCanvas.stroke();
                progressCanvas.closePath();
                progressCanvas.clearRect(100, 100, 100, 100);
                progressCanvas.fillStyle = '#0099ff';
                progressCanvas.font = '36px Consolas';
                progressCanvas.fillText(text, 150 - text.length * 10, 150 + 9);
            },
            done : function(e, data)
            {
                var result = data.result;
                if (result.error.code) warning('文件上传失败: ' + result.error.code);
                greeting('文件上传成功');
                showPath(currentPath);
            },
            fail : function()
            {
                warning('文件上传失败');
                $('.x-progress').hide();
            }
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
    // 文件管理相关
    _showFiles : function(result)
    {
        // 文件名排序
        result.files.sort(function(a, b)
        {
            if (a.isDirectory && b.isDirectory == false) return -1;
            else if (a.isDirectory == false && b.isDirectory) return 1;
            else return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;
        });

        var shtml = '';
        for (var i = 0; i < result.files.length; i++)
        {
            var f = result.files[i];
            var name = f.name;
            var sname = '';
            for (var d = 0, s = 0; d < name.length; d++)
            {
                var chr = name.charAt(d);
                sname += chr;
                s += chr > 0x7f ? 2 : 1;
                if (s > 40)
                {
                    sname += '..';
                    break;
                }
            }
            sname = name;
            var suffix = f.isDirectory ? null : name.indexOf('.') > -1 ? name.substring(name.lastIndexOf('.') + 1) : null;
            var fileTypeInfo = f.isDirectory ? FileTypes.folder : FileTypes.get(suffix);
            var fileIcon = fileTypeInfo.icon;
            var flength = f.length;
            if (flength > 1024 * 1024 * 1024) flength = (flength / 1024 / 1024 / 1024).toFixed(2) + 'G';
            else if (flength > 1024 * 1024) flength = (flength / 1024 / 1024).toFixed(2) + 'M';
            else if (flength > 1024) flength = (flength / 1024).toFixed(2) + 'k';
            else flength = flength + 'b';
            var mtime = new Date(f.mtime).toLocaleString();
            shtml += '<tr>';
            shtml += '  <td><i><img src="' + RES_PATH + '/ftype/' + fileIcon + '" /></i><' + (f.isDirectory ? 'a href="javascript:;"' : 'span') + ' x-name="' + escape(f.name) + '" ' + (f.isDirectory ? 'class="x-dir"' : '') + '>' + sname + '</' + (f.isDirectory ? 'a' : 'span') + '></td>';
            shtml += '  <td align="center">' + (fileTypeInfo == null ? '-' : fileTypeInfo.name) + '</td>';
            shtml += '  <td align="right">' + flength + '</td>';
            shtml += '  <td align="center">' + (f.isDirectory ? '' : '<a target="_blank" href="javascript:;" id="x-download-file" x-fname="' + encodeURIComponent(f.name) + '"><img src="../static/icon/download.png" /></a>') + '</td>';
            shtml += '</tr>';
        }
        $('.x-fmanager table tbody').html(shtml);
        setTimeout(function()
        {
            $('.x-fmanager .x-filelist').get(0).scrollTop = '0px';
        }, 0);
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

    // /////////////////////////////////////////////////////////////////////
    // 杂项

    // 每5秒发送一个HTTP请求，以保持会话
    __keepalive : function()
    {
        var self = this;
        $.post(ROOT_PATH + '/keepalive', null, null);
        this._send({
            type : 'command',
            command : 'keepalive',
        });
        setTimeout(function()
        {
            self.__keepalive();
        }, 20000);
    }
};