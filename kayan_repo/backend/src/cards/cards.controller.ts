import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { CardsService } from './cards.service';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { PaginationDto } from '../common/dto/pagination.dto';

@ApiTags('Cards')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('cards')
export class CardsController {
  constructor(private readonly cardsService: CardsService) {}

  @Get()
  @ApiOperation({ summary: 'Get all cards with pagination & filtering' })
  @ApiQuery({ name: 'isUsed', required: false, type: Boolean })
  findAll(
    @CurrentUser() user: any, 
    @Query() query: PaginationDto,
    @Query('isUsed') isUsed?: boolean,
  ) {
    return this.cardsService.findAll(user.tenantId, query, isUsed);
  }
}
