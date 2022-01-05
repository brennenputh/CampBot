FROM ibm-semeru-runtimes:open-17-jre-focal

WORKDIR /usr/app
COPY build/install/campbot/ .

LABEL org.opencontainers.image.source = "https://github.com/AMereBagatelle/CampBot"

CMD ["-Duser.dir=/home/campbot/"]
ENTRYPOINT ["bin/campbot"]