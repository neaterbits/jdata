/**
 * Base64 implementation for loading images from data instead of from URLs
 */

// For base-64 encoding bytes
function base64_encode(dataView, start, length) {

	const base64chars = [
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'+', '/'
	];

	var result = "";
	
	var buf = { buffer : 0, bufferedBits : 0 };
	
	for (var i = 0; i < length;) {
		
		// console.log('start of loop, ' + i + ': ' + buf.buffer + '/0x' + hexNum(buf.buffer) + ', ' + buf.bufferedBits);
		
		if (buf.bufferedBits <= 8) {
			// May read another byte, shift remaining up to upper
			
			var b = dataView.getUint8(start + i);
			
			// or in read byte after current number of bits
			var freeSpace = 16 - buf.bufferedBits;
			var r = b << (freeSpace - 8);

			buf.buffer |= r;
			
			// Now is 8 more
			buf.bufferedBits += 8;
		
			// One more element processed
			++ i;
		}

		// console.log('after read, ' + i + ': ' + buf.buffer + '/0x' + hexNum(buf.buffer) + ', ' + buf.bufferedBits);
		
		result += _base64_one(buf, base64chars);
	}
	
	// Now convert any remaining data
	while (buf.bufferedBits > 0) {
		result += _base64_one(buf, base64chars);
	}
	
	var mod = result.length % 4;
	if (mod != 0) {
		var remainder = 4 - remainder;

		for (var i = 0; i < remainder; ++ i) {
			result += '=';
		}
	}

	return result;
}

function _base64_one(buf, base64chars) {

	// Can now encode from bits
	var c = buf.buffer >> 10;
	// console.log('Encode 0x' + hexNum(c) + ' : ' + base64chars[c]);
	
	if (c < 0 || c >= 64) {
		throw "char to be encoded out of range: " + c;
	}

	// Got 6 uppermost bits, so skip those
	buf.buffer = (buf.buffer << 6) & 0x0000FFFF;
	buf.bufferedBits -= 6;

	var s = base64chars[c];
	
	if (typeof s === 'undefined') {
		throw 'Undefined for ' + c;
	}
	
	return s;
}
