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

function test4(y)
{
	while(true)
	{
		y = y * 2;
		y = y / 2;
	}
}

function test5()
{
	var x;
	do{
		x = 6;
	}while(x == 6);
}

function test6()
{
    var x;
    for(var i = 0; i < 3;i++)
    {
        x += i;
    }
}

//teste atual
function test7()
{
    var x = 2;
    var y = 1.0;
    var z = true;
    var a = "ola";
    var b = 'a';
}

//teste atual
function test8()
{
    var x = 2;
    var y = 1.0;
    var z;
    z = y * x;
}