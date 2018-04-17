<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>List test</title>
</head>
<body>

<div>
	<ul>
		<li>
			<div>
				<div>item1</div>
				<div id = "test_div" style='overflow: auto;'>
					<div>

					<!-- 
							<div>item1sub1</div>
							<div>item1sub2</div>
							<div>item1sub3</div>
					 -->
					
						<ul >
							<li>item1sub1</li>
							<li>item1sub2</li>
							<li>item1sub3</li>
						</ul>
					</div>
				</div>
			</div>
		</li>
		<li>
			<div>item2</div>
		</li>
		<li><div>item3</div></li>
	</ul>
</div>
</body>

<script type="text/javascript">
	window.onload = function() {
		document.getElementById('test_div').style['height'] = '0px';
		
		setTimeout(function() {
			document.getElementById('test_div').style['height'] = 'auto';
		},
		5000)
	}
</script>

</html>