<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>

 <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">

    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datetimepicker/4.17.37/css/bootstrap-datetimepicker.min.css" />

    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.6/moment.min.js"></script>   
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/fr.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>


<script>
	$(function () {
		$('#datetimepicker2').datetimepicker({
			format: 'YYYY-MM-DD'
		});
		$('#datetimepicker3').datetimepicker({
			format: 'LT'
		});
		$('#datetimepicker4').datetimepicker({
			format: 'LT'
		});

	});
</script>	

</head>
<body>

			<div class="row">
				<div class='col-sm-4'>
					<div class="form-group">
						<div class='input-group date' id='datetimepicker3'>
							<input type='text' class="form-control" /> <span
								class="input-group-addon"> <span
								class="glyphicon glyphicon-time"></span>
							</span>
						</div>
					</div>
				</div>
				<div class='col-sm-4'>
					<div class="form-group">
						<div class='input-group date' id='datetimepicker4'>
							<input type='text' class="form-control" /> <span
								class="input-group-addon"> <span
								class="glyphicon glyphicon-time"></span>
							</span>
						</div>
					</div>
				</div>
			</div>

</body>
</html>