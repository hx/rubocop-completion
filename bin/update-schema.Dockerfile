FROM ruby:3
RUN gem install rubocop-schema-gen -v '~> 0.1.5'
CMD rubocop-schema-gen --build-repo=/repo
