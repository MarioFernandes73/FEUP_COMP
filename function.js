//http://esprima.org/demo/parse.html#

function test1()
{
	var answer = 6 * 7;
	answer = answer + 1;
}

function test2(i,j)
{
	var x = [0,1,2];
	x[i-1] = x[0] + 1;
	return j;
}

//teste atual
function test3(y)
{
	var x;
	if(x < y)
	{
		x = y;
		x = x + 4;
	}
	else{
		x = y;
		x = x + 6;
	}
	
	return x;
}