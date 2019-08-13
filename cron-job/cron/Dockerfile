FROM ubuntu:latest
MAINTAINER docker@ekito.fr

RUN apt-get update && apt-get -y install cron
RUN apt-get update && apt-get -y install curl

# Copy hello-cron file to the cron.d directory
COPY monthly-charge-invoices.sh /etc/cron.d/monthly-charge-invoices.sh

# Give execution rights on the cron job
RUN chmod 0644 /etc/cron.d/monthly-charge-invoices.sh

# Apply cron job
RUN crontab /etc/cron.d/monthly-charge-invoices.sh

# Create the log file to be able to run tail
RUN touch /var/log/cron.log

# Run the command on container startup
CMD ["cron", "-f"]
