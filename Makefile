default: build

clean:
	clojure -T:build clean

build:
	clojure -T:build build

test:
	clojure -T:build test

ci:
	clojure -T:build ci

install:
	clojure -T:build install

deploy:
	@export CLOJARS_USERNAME=$(shell cat ~/.clojars/username) && \
	export CLOJARS_PASSWORD=$(shell cat ~/.clojars/token) && \
	clojure -T:build deploy

publish: ci deploy

.PHONY: clean build test ci install deploy publish