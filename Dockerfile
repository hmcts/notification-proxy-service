ARG APP_INSIGHTS_AGENT_VERSION=3.4.14
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/notifications-service.jar /opt/app/

EXPOSE 8080
CMD [ \
    "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
    "notifications-service.jar" \
    ]
