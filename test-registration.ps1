# PowerShell script to test student registration with email

$headers = @{
    "Content-Type" = "application/json"
}

$studentBody = @{
    name = "John Doe"
    email = "your-email@gmail.com"  # Replace with your actual email
    password = "test123"
    phoneNumber = "+1234567890"
    college = "Stanford University"
    hostelName = "West Campus"
    roomNumber = "A-204"
    address = "123 Campus Road"
} | ConvertTo-Json

$chefBody = @{
    name = "Chef Gordon"
    email = "your-email@gmail.com"  # Replace with your actual email
    password = "test123"
    phoneNumber = "+1234567891"
    specialization = "Italian Cuisine"
    experienceYears = 5
    bio = "Passionate chef with 5 years experience in Italian cuisine"
} | ConvertTo-Json

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "     Testing Student Registration with Email      " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register/student" `
                                  -Method Post `
                                  -Headers $headers `
                                  -Body $studentBody
    
    Write-Host "✅ Student Registration Successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 3 | Write-Host
    Write-Host ""
    Write-Host "📧 Check your email inbox for the welcome email!" -ForegroundColor Cyan
    Write-Host "   (Also check spam/junk folder)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Error:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "     Testing Chef Registration with Email         " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register/chef" `
                                  -Method Post `
                                  -Headers $headers `
                                  -Body $chefBody
    
    Write-Host "✅ Chef Registration Successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 3 | Write-Host
    Write-Host ""
    Write-Host "📧 Check your email inbox for the welcome email!" -ForegroundColor Cyan
    Write-Host "   (Also check spam/junk folder)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Error:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}
