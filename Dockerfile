FROM registry.redhat.io/rhdm-7/rhdm-kieserver-rhel8:7.7.1
COPY target/extend-capability-1.0-SNAPSHOT.jar /opt/eap/standalone/deployments/ROOT.war/WEB-INF/lib/