var FileTypes = {
    types : {
        'msaccess' : { name : 'Microsoft Access数据库', icon : 'access.ico', suffix : 'accdb,mdb' },
        'audio' : { name : '音频文件', icon : 'audio.ico', suffix : 'mp3,wav,midi,cd,ogg,asf,wma,mp3pro,ape,vqf' },
        'bittorrent' : { name : 'BT种子文件', icon : 'bt.ico', suffix : 'torrent' },
        'cert' : { name : '证书文件', icon : 'cert.ico', suffix : 'cer' },
        'chm' : { name : '帮助文档', icon : 'chm.ico', suffix : 'chm' },
        'config' : { name : '', icon : 'config.ico', suffix : 'css,properties,conf,config,ini' },
        'exe' : { name : '可执行程序', icon : 'exe.ico', suffix : 'exe,com' },
        'link' : { name : '快捷方式', icon : '', suffix : 'lnk,desktop' },
        'folder' : { name : '文件夹', icon : 'folder.ico', suffix : '-d-' },
        'font' : { name : '字体文件', icon : 'font.ico', suffix : 'ttf,otf,ttc' },
        'html' : { name : 'HTML网页', icon : 'html.ico', suffix : 'html,htm,hta,xhtml,shtml,shtm' },
        'image' : { name : '光盘映像文件', icon : 'image.ico', suffix : 'iso,img,bin,nrg,vcd,cif,fcd,ccd,c2d,dfi,tao,dao,cue' },
        'java' : { name : 'Java源代码', icon : 'java.ico', suffix : 'java' },
        'markdown' : { name : 'Markdown文档', icon : 'md.ico', suffix : 'md' },
        'msppt' : { name : 'Microsoft PowerPoint演示文档', icon : 'msppt.ico', suffix : 'ppt,pptx' },
        'msproject' : { name : 'Microsoft Project项目文档', icon : 'msproject.ico', suffix : 'mpp' },
        'msword' : { name : 'Microsoft Word文档', icon : 'msword.ico', suffix : 'doc,docx' },
        'msexcel' : { name : 'Microsoft Excel表格', icon : 'msxls.ico', suffix : 'xls,xlsx' },
        'pdf' : { name : '便携式文档', icon : 'pdf.ico', suffix : 'pdf' },
        'picture' : { name : '图像文件', icon : 'pictures.ico', suffix : 'png,bmp,jpg,jpeg,gif,dib,jpe,jfif' },
        'python' : { name : 'Python脚本', icon : 'py.ico', suffix : 'py' },
        'axurerp' : { name : 'AxureRP设计文档', },
        'script' : { name : '脚本文件', icon : 'script.ico', suffix : 'js,sh,bat,vbs' },
        'sql' : { name : 'SQL脚本', icon : 'sql.ico', suffix : 'sql' },
        'plaintext' : { name : '文本文件', icon : 'txt.ico', suffix : 'txt' },
        'video' : { name : '视频文件', icon : 'video.ico', surffix : 'avi,divx,asf,asx,wm,wmp,wmv,wmx,wvx,m1v,m2v,mpe,mpeg,mpg,mkv,rm,rmvb,mov,mqv,mp4,m4v,lmp4,3gp,k3g,mpv2,mp2v,m4p,m4b,3gpp,3g2,3gp2' },
        'msvisio' : { name : 'Microsoft Visio文档', icon : 'visio.ico', suffix : 'vsd,vsdx' },
        'xmind' : { name : 'Xmind脑图', icon : 'xmind.ico', suffix : 'xmind' },
        'xml' : { name : 'XML文档', icon : 'xml.ico', suffix : 'xml,xsd' },
        'zip' : { name : '压缩文件', icon : 'zip.ico', suffix : 'gz,zip,bz2,7z,rar,gzip,cab' },
    },
    unknownType : { name : '', icon : 'unknown.ico', suffix : null },
    mappings : null,
    get : function(extname)
    {
        if (null == this.mappings) this._map();
        var type = this.mappings[extname.toLowerCase()];
        if (type) return this.types[type];
        else return this.unknownType;
    },
    _map : function()
    {
        if (this.mappings != null) return;
        this.mappings = {};
        for (var j in this.types)
        {
            var type = this.types[j];
            if (typeof(type.name) == 'undefined') continue;
            var exts = type.suffix.split(',');
            for (var i = 0; i < exts.length; i++)
            {
                this.mappings[exts[i]] = j;
            }
        }
    }
}