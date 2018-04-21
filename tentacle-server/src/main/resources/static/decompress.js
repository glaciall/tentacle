/**
 * 图像解压，返回canvas所需的imageData
 * @param method
 * @param compressedData
 */
var xxoo = false;
function decompress(method, compressedData, imageData)
{
    // 行程编码解码
    var f = 0;
    var x = 0;
    if (xxoo) console.log(compressedData);
    xxoo = false;
    var headerLength = 16;
    /*
    var width = ((compressedData[0] << 8) | compressedData[1]) & 0xffff;
    var height = ((compressedData[2] << 8) | compressedData[3]) & 0xffff;
    var x = '';
    for (var i = 4; i < 12; i++)
    {
        x = x + ('00' + compressedData[i].toString(16)).replace(/^0+(\w{2})$/gi, '$1');
    }
    var captureTime = parseInt(x, 16);
    */
    var sequence = (compressedData[12] << 24 | compressedData[13] << 16 | compressedData[14] << 8 | compressedData[15]) & 0xffffffff;
    console.log("screen sequence: " + sequence);
    for (var i = (compressedData[headerLength] & 0xff) * 3 + 1 + headerLength, k = 0; i < compressedData.length; )
    {
        var rl = (((compressedData[i] & 0xff) << 8) | (compressedData[i + 1] & 0xff)) & 0xffff
        var times = rl & 0x7fff;
        var red, green, blue;
        if ((rl & 0x8000) > 0)
        {
            var index = compressedData[i + 2] & 0xff;
            if (index == 0)
            {
                k += times * 4;
                i += 3;
                continue;
            }
            var index = (index - 1) * 3 + 1 + headerLength;
            red = compressedData[index] & 0xff;
            green = compressedData[index + 1] & 0xff;
            blue = compressedData[index + 2] & 0xff;
            i += 3;
        }
        else
        {
            red = compressedData[i + 2] & 0xff;
            green = compressedData[i + 3] & 0xff;
            blue = compressedData[i + 4] & 0xff;
            i += 5;
        }

        for (var s = 0; s < times; s++)
        {
            imageData.data[k++] = red;
            imageData.data[k++] = green;
            imageData.data[k++] = blue;
            imageData.data[k++] = 0xff;
        }
    }
}