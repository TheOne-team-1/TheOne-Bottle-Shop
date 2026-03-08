dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env.dev up -d

down:
	docker compose down

down-v:
	docker compose down -v