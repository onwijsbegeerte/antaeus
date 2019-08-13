## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.


*Running through docker*

Install docker for your platform

```
make docker-run
```

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```


### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the "rest api" models used throughout the application.
|
├── pleo-antaeus-rest
|        Entry point for REST API. This is where the routes are defined.
└──
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!

# Docs

## Main flow

1. Cron-job registered in Kubernetes for the first of the month
2. First of the month, a shell script would be doing a simple CURL command inside of the netwerk to Antaeus to ```/rest/v1/invoices/chargeall```
3. The call gets routed to the BillingService attempting to charge all unpaid invoices
4. Each attempt will add an "Attempted Charge Status" for that invoice to the database
5. On success, an "Success Charge Status" is added, and the invoice is updated to "Paid" in the database
6. On failure, "Failed Status" will get written the the Charge Status database
7. For all the failed invoices: they get retried, step 3-7 will be run again, but with a limit of 4 retries. Then the service exits, responding to the caller with the unpaid invoices.
8. At the first of each month, step 1 to 8 will be ran again.

## To test yourself

1. Run the application
2. When it is running, CURL to ```/rest/v1/invoices/chargestatuses```
3. When an empty object is returned it means success: all the invoices were charged. If that is not the case, the pending invoices will be returned.
4. You can investigate what happened by using the ```/rest/v1/invoices/chargestatus/{:id}``` endpoint. You’ll see that 4 attempts were made, but they all failed.
5. Call ```rest/v1/invoices/chargestatuses``` again, to attempt an additional charge.


## Docker cronjob

In the ```./cron-job``` directory of Antaeus is a small example of a container running a cronjob. Although I think managing it in Kubernetes would be better.
Executing ```docker-compose up``` in ```./cron-job``` will run the "monthly-charge-invoice" bash script, a really simple curl from within the network to the Antheus charge endpoint. For demo-purposes it runs every
minute. If you uncomment the monthly cronjob instead, it will run on the first of each month.


## Notes/Considerations

I first looked at using the ScheduledExecutorService from the Java library. Although it was flexible enough to do it in theory, there were a couple of things I didn’t like.

First, I don’t like programming actual cronjobs inside of the server. I thought it was mixing the responsibilities quite a bit. Is Antaeus a web server for dealing with invoices and customers only, or is it also responsible for dealing with subscriptions? I thought Antaeus was more like the former.

So I decided that I didn’t want any scheduling logic within the application itself. I was thinking how I’ve seen it done in other projects I worked on. The most common way is to add cronjobs to the vm’s or use cloud services to manage it. For example, I know you can create a simple Azure function cronjob.

In this case I thought it might be neat to keep it really simple, by adding a cronjob to kubernetes, which calls a simple container that is responsible for “all” the cronjobs (or just this one).

One of my main concerns with any financial application is traceability. I didn’t want to make a fire-and-forget service. I’ve added a bit of logic to the application (InvoiceChargeStatusTable) to keep track of what charge attempts are made.

Also, building on that database table, I added some logic that keeps track of charges, and stops charging based on the current state. I also added a couple of endpoints to deal with those “invoice charge statuses”.

## Improvements

There are quite a bit of things that I think could be improved:

- The route names and structures could be improved, also charging the invoices is currently a GET, but could be a POST.

- Dealing with dates was a bit harder than I expected, I would have liked to add a bit more logic to the "InvoiceChargeLimiter". However, I ran out of time dealing with the ORM datetimes and the native Java datatimes.

- The BillingService is a bit messy; it could use some more time to clean it up more. I’ve used some mutability, but ideally it would be immutable. However, I've never worked worked with Kotlin before, and I didn’t write more than 20 lines of Java (If you don’t count auto-generating Android boilerplate code). Also, I don’t like how each function deals with side effects currently, instead of having a few pure functions that deal with the logic, and some methods that deal with all the side effects.

- There are a few gaps that I would have normally asked a business analyst/expert about:

- - You asked to charge on the first of each month; I’ve noticed that the customers are spread around the world. However, I didn’t factor in timezones, as I couldn't find a solid metric to identify a unique timezone (currency is not enough).
  - I wasn’t sure how to deal with the currencies. I did notice some 'currency mismatch'-exceptions, but I wasn’t 100% positive that it would be possible in our application. For example, I’m Dutch, yet some of my invoices I pay in euros but others in dollars.

- I think there are still plenty things that are currently not tested.
- Primitives like CustomerId, InvoiceId etc are currently easily swapped, they could use a wrapper type to make sure they are not mixed up.
- Proper exception handling is missing, but I'm not sure about the best practices in Kotlin. However, I assume exception handling or having some kind of more explicit result type would make the application better.

Looking forward to talk to you about what I've created, and to get your thoughts on my decisions!

