/**
 * 图像解压，返回canvas所需的imageData
 * @param method
 * @param compressedData
 */
function decompress(method, compressedData, imageData)
{
    // 行程编码解码
    console.log(compressedData);
    var f = 0;
    var x = 0;
    for (var i = (compressedData[0] & 0xff) * 3, k = 0; i < compressedData.length; )
    {
        var rl = compressedData[i] & 0xff;
        var times = rl & 0x7f;
        var red, green, blue;
        if ((rl & 0x80) > 0)
        {
            var index = (compressedData[i + 1] & 0xff) * 3 + 1;
            red = compressedData[index] & 0xff;
            green = compressedData[index + 1] & 0xff;
            blue = compressedData[index + 2] & 0xff;
            if (x++ < 100) console.log('need colortable');
        }
        else
        {
            red = compressedData[i + 1] & 0xff;
            green = compressedData[i + 2] & 0xff;
            blue = compressedData[i + 3] & 0xff;
        }

        for (var s = 0; s < times; s++)
        {
            imageData.data[k++] = red;
            imageData.data[k++] = green;
            imageData.data[k++] = blue;
            imageData.data[k++] = 0xff;
            if (f++ < 100) console.log(((red << 16) | (green << 8) || blue).toString(16));
        }
        i += (rl & 0x80) > 0 ? 2 : 4;
    }
}