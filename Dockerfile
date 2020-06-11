FROM registry.redhat.io/rhdm-7/rhpam-kieserver-rhel8:7.8.0
COPY target/extend-capability-1.0-SNAPSHOT.jar /opt/eap/standalone/deployments/ROOT.war/WEB-INF/lib/