import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { Injectable, Logger } from '@nestjs/common';
import { TransactionsService } from '../transactions/transactions.service';

@Injectable()
@WebSocketGateway({ path: '/ws', cors: { origin: '*' } })
export class EventsGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private readonly logger = new Logger(EventsGateway.name);

  constructor(private readonly transactionsService: TransactionsService) {}

  handleConnection(client: Socket) {
    this.logger.log(`Client connected: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`Client disconnected: ${client.id}`);
  }

  @SubscribeMessage('syncSms')
  async handleSyncSms(@MessageBody() payload: any, @ConnectedSocket() client: Socket) {
    this.logger.log(`Received SMS sync from ${client.id}:`, payload);
    // Here we can process the SMS payload from the Android agent,
    // update the database, and emit events back to the web dashboard.
    
    // Echo acknowledgment
    client.emit('ack', { status: 'received', timestamp: new Date().toISOString() });

    // Broadcast to web dashboards
    this.server.emit('dashboardUpdate', { type: 'NEW_TRANSACTION', data: payload });
  }

  // Method to push updates from NestJS to connected clients (like Admin Dashboard or Android)
  broadcastUpdate(event: string, data: any) {
    this.server.emit(event, data);
  }
}
