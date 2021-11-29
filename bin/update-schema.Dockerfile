FROM ruby:3
RUN gem install rubocop-schema-gen -v '~> 0.1.6'
CMD rubocop-schema-gen --build-repo=/repo
