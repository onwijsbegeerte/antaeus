# every minute
* * * * * curl http://web:7000/rest/v1/invoices/chargeall > /proc/1/fd/1 2>/proc/1/fd/2
# first day of the month 12:00
# 0 0 1 * * curl http://web:7000/rest/v1/invoices/chargeall > /proc/1/fd/1 2>/proc/1/fd/2
# An empty line is required at the end of this file for a valid cron file.